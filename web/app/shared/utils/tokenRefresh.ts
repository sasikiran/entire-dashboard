/**
 * Token refresh manager
 * Handles token auto-refresh logic, avoids concurrent refresh issues
 */

import { authApi } from '~/features/auth/api/auth.api'
import { saveAccessToken, removeAccessToken } from './tokenStorage'
import { ROUTES } from '~/shared/constants/routes'

/**
 * Refresh state management
 */
let isRefreshing = false
let refreshPromise: Promise<string> | null = null

/**
 * Refresh Access Token
 * Uses Promise cache to ensure only one refresh at a time for concurrent requests
 */
export async function refreshAccessToken(): Promise<string> {
  // If already refreshing, return existing Promise
  if (isRefreshing && refreshPromise) {
    console.log('[Token] Waiting for existing refresh request to complete...')
    return refreshPromise
  }

  // Start new refresh flow
  isRefreshing = true
  console.log('[Token] Starting to refresh Access Token...')

  refreshPromise = (async () => {
    try {
      // Call refresh API (refreshToken is automatically sent in Cookie)
      const response = await authApi.refreshToken()
      
      // Save new token and expiration time
      saveAccessToken(response.accessToken, response.expire)
      
      console.log('[Token] Access Token refreshed successfully')
      return response.accessToken
    } catch (error: any) {
      console.error('[Token] Refresh failed:', error)
      
      // Refresh failed, clear all auth info
      handleRefreshFailure()
      
      throw error
    } finally {
      // Reset refresh state
      isRefreshing = false
      refreshPromise = null
    }
  })()

  return refreshPromise
}

/**
 * Handle refresh failure
 * Clear all auth info and redirect to login page
 */
function handleRefreshFailure(): void {
  console.log('[Token] Refresh Token expired, redirecting to login page')
  
  // Clear local storage
  removeAccessToken()
  
  // Clear user state
  if (import.meta.client) {
    useState('auth:user').value = null
  }
  
  // Redirect to login page
  navigateTo(ROUTES.LOGIN)
}

/**
 * Reset refresh state (for testing or special scenarios)
 */
export function resetRefreshState(): void {
  isRefreshing = false
  refreshPromise = null
}
