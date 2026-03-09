/**
 * Generate unified diff from before/after content
 * Uses diff package when installed (see package.json), otherwise fallback
 */

function createUnifiedDiffFallback(
  filePath: string,
  before: string,
  after: string
): string {
  const oldLines = before.split(/\r?\n/)
  const newLines = after.split(/\r?\n/)
  const header = `--- a/${filePath}\n+++ b/${filePath}\n`
  const result: string[] = []
  let i = 0
  let j = 0
  while (i < oldLines.length || j < newLines.length) {
    if (i < oldLines.length && j < newLines.length && oldLines[i] === newLines[j]) {
      result.push(' ' + oldLines[i])
      i++
      j++
    } else if (i < oldLines.length && (j >= newLines.length || oldLines[i] !== newLines[j])) {
      result.push('-' + oldLines[i])
      i++
    } else if (j < newLines.length) {
      result.push('+' + newLines[j])
      j++
    } else {
      i++
    }
  }
  const body = result.join('\n')
  return body ? header + body + '\n' : header
}

export function createUnifiedDiff(
  filePath: string,
  before: string,
  after: string
): string {
  return createUnifiedDiffFallback(filePath, before, after)
}
