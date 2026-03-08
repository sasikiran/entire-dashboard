/**
 * Admin API client utility
 * Unified handling of admin API requests
 */

import { useApi, $api } from './api-client'

/**
 * Admin API base path
 */
const OPEN_API_BASE = '/api/v1/open'

/**
 * useFetch wrapper dedicated for admin
 * Automatically adds auth header and /api/v1/open prefix
 */
export function useOpenApi<T = any>(path: string | (() => string), options?: Parameters<typeof useFetch<T>>[1]) {
  const url = typeof path === 'function' ? () => `${OPEN_API_BASE}${path()}` : `${OPEN_API_BASE}${path}`

  return useApi<T>(url, options)
}

/**
 * $fetch wrapper dedicated for admin
 * Automatically adds auth header and /api/v1/open prefix
 */
export function $openApi<T>(path: string, options?: Parameters<typeof $fetch>[1]): Promise<T> {
  return $api<T>(`${OPEN_API_BASE}${path}`, options)
}
