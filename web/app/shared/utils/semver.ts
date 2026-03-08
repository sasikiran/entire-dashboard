/**
 * Semantic version comparison (only supports x.y.z format)
 * Non-semver format is considered invalid, no update prompt.
 */

const SEMVER_REGEX = /^\d+\.\d+\.\d+$/

/**
 * Check if it's a valid x.y.z version number
 */
export function isValidSemver(version: string): boolean {
  if (!version || typeof version !== 'string') return false
  return SEMVER_REGEX.test(version.trim())
}

/**
 * Parse x.y.z to [major, minor, patch]
 */
function parse(version: string): [number, number, number] | null {
  if (!isValidSemver(version)) return null
  const parts = version.trim().split('.').map(Number)
  return [parts[0], parts[1], parts[2]]
}

/**
 * Check if latest is strictly greater than current (only compares when both are valid semver)
 */
export function isVersionNewer(latest: string, current: string): boolean {
  const a = parse(latest)
  const b = parse(current)
  if (!a || !b) return false
  if (a[0] !== b[0]) return a[0] > b[0]
  if (a[1] !== b[1]) return a[1] > b[1]
  return a[2] > b[2]
}
