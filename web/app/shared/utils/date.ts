/**
 * Date utility functions
 */

/**
 * Get date N days ago (time set to 00:00:00)
 */
export function getDaysAgo(days: number): Date {
  const date = new Date()
  date.setDate(date.getDate() - days)
  date.setHours(0, 0, 0, 0)
  return date
}

/**
 * Get end of today (23:59:59)
 */
export function getEndOfToday(): Date {
  const date = new Date()
  date.setHours(23, 59, 59, 999)
  return date
}

/**
 * Convert date to millisecond timestamp
 */
export function dateToTimestamp(date: Date): number {
  return date.getTime()
}

/**
 * Format date as YYYY-MM-DD
 */
export function formatDate(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * Parse date from YYYY-MM-DD string
 */
export function parseDate(dateStr: string): Date {
  const date = new Date(dateStr)
  date.setHours(0, 0, 0, 0)
  return date
}

/**
 * Generate all dates (YYYY-MM-DD) between startTime and endTime inclusive, in local timezone
 */
export function getDateRange(startTime: number, endTime: number): string[] {
  const dates: string[] = []
  const start = new Date(startTime)
  const end = new Date(endTime)
  start.setHours(0, 0, 0, 0)
  end.setHours(0, 0, 0, 0)
  const cur = new Date(start)
  while (cur <= end) {
    dates.push(formatDate(cur))
    cur.setDate(cur.getDate() + 1)
  }
  return dates
}

/**
 * Validate date range
 */
export function validateDateRange(startDate: Date, endDate: Date): { valid: boolean; error?: string } {
  // Start date must be less than end date
  if (startDate >= endDate) {
    return { valid: false, error: 'Start date must be less than end date' }
  }

  return { valid: true }
}
