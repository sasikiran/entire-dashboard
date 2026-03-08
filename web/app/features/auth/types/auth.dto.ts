/**
 * Auth module - Backend interface contract (DTO)
 */

/**
 * Login request
 */
export interface LoginDTO {
  username: string
  password: string
}

/**
 * Login response
 */
export interface FullAuthUserDTO {
  username: string
  accessToken: string
  expire: number  // Access Token expiration time (millisecond timestamp)
  refreshToken: string  // Although stored in Cookie, backend will return it
  refreshExpire: number  // Refresh Token expiration time (millisecond timestamp)
}

/**
 * Token refresh response
 */
export interface RefreshTokenResponse {
  username: string
  accessToken: string
  expire: number
  refreshToken: string
  refreshExpire: number
}
