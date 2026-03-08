/**
 * API client dedicated to Token refresh
 * Does not use auto-refresh logic to avoid circular calls
 */

/**
 * $fetch wrapper dedicated for refreshing Token
 * Will not trigger auto-refresh logic to avoid infinite loops
 */
export function $refreshApi<T>(url: string, options?: Parameters<typeof $fetch>[1]): Promise<T> {
  return $fetch<T>(url, {
    ...options,
    // Do not add Authorization header because refreshToken is in Cookie
    credentials: 'include', // Ensure Cookie is sent
  })
}
