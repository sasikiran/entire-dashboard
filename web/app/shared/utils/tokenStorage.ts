/**
 * Token storage utility
 */

import { STORAGE_KEYS } from '~/shared/constants/config'

/**
 * Save Access Token and expiration time
 */
export function saveAccessToken(token: string, expire?: number): void {
  if (import.meta.client) {
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, token)
    if (expire) {
      localStorage.setItem(STORAGE_KEYS.TOKEN_EXPIRE, expire.toString())
    }
  }
}

/**
 * Get Access Token
 */
export function getAccessToken(): string | null {
  if (import.meta.client) {
    return localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN)
  }
  return null
}

/**
 * Get Token expiration time
 */
export function getTokenExpire(): number | null {
  if (import.meta.client) {
    const expire = localStorage.getItem(STORAGE_KEYS.TOKEN_EXPIRE)
    return expire ? parseInt(expire, 10) : null
  }
  return null
}

/**
 * Check if Token is expired
 */
export function isTokenExpired(): boolean {
  const expire = getTokenExpire()
  if (!expire) return true
  
  // Determine as expired 30 seconds in advance to avoid edge cases
  return Date.now() >= expire - 30000
}

/**
 * Remove all Token related data
 */
export function removeAccessToken(): void {
  if (import.meta.client) {
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN)
    localStorage.removeItem(STORAGE_KEYS.TOKEN_EXPIRE)
  }
}

