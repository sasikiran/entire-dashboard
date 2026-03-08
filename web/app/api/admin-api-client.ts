/**
 * Admin API client utility
 * Unified handling of admin API requests
 */

import { useApi, $api } from './api-client'

/**
 * Admin API base path
 */
const ADMIN_API_BASE = '/api/v1/admin'

/**
 * useFetch wrapper dedicated for admin
 * Automatically adds auth header and /api/v1/admin prefix
 */
export function useAdminApi<T = any>(path: string | (() => string), options?: Parameters<typeof useFetch<T>>[1]) {
  const url = typeof path === 'function' 
    ? () => {
        const result = path()
        return result ? `${ADMIN_API_BASE}${result}` : null
      }
    : `${ADMIN_API_BASE}${path}`

  return useApi<T>(url, options)
}

/**
 * $fetch wrapper dedicated for admin
 * Automatically adds auth header and /api/v1/admin prefix
 */
export function $adminApi<T>(path: string, options?: Parameters<typeof $fetch>[1]): Promise<T> {
  return $api<T>(`${ADMIN_API_BASE}${path}`, options)
}
