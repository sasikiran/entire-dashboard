/**
 * Browser utility functions
 */

/**
 * Open URL in new tab with security attributes (rel="noopener noreferrer")
 */
export function openExternal(url: string): void {
  if (!url) return
  const w = window.open(url, '_blank', 'noopener,noreferrer')
  if (w) w.opener = null
}
