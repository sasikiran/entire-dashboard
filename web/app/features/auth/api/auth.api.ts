/**
 * Auth module - API calls
 */
import { $openApi } from '~/api/open-api-client'
import { $refreshApi } from '~/api/refresh-api-client'
import type { LoginDTO, FullAuthUserDTO, RefreshTokenResponse } from '../types/auth.dto'

const OPEN_API_BASE = '/api/v1/open'

export const authApi = {
    /**
     * Login
     */
    login: (data: LoginDTO) =>
        $openApi<FullAuthUserDTO>('/auth/login', {
            method: 'POST',
            body: data,
        }),

    /**
     * Logout
     */
    logout: () =>
        $openApi<void>('/auth/logout', {
            method: 'POST',
        }),

    /**
     * Refresh Token
     * Note: refreshToken is stored in HttpOnly Cookie and will be sent automatically
     * Use dedicated API client to avoid circular refresh
     */
    refreshToken: () =>
        $refreshApi<RefreshTokenResponse>(`${OPEN_API_BASE}/auth/refresh`, {
            method: 'POST',
        }),
}
