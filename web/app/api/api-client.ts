import {getAccessToken, removeAccessToken} from "~/shared/utils/tokenStorage"
import {refreshAccessToken} from "~/shared/utils/tokenRefresh"
import {ROUTES} from "~/shared/constants/routes"
import type {ApiErrorResponse} from "~/shared/types/api"
import type { NitroFetchRequest } from 'nitropack'


/**
 * Handle 401 unauthorized response
 * Clear local token, user state and redirect to login page
 */
function handleUnauthorized() {
    if (import.meta.client) {
        removeAccessToken()
        useState('auth:user').value = null
        navigateTo(ROUTES.LOGIN)
    }
}

/**
 * Display error toast
 * Choose appropriate toast type based on HTTP status code
 */
function displayErrorToast(error: any) {
    if (!import.meta.client) return

    const message = useMessage()
    const status = error?.response?.status || error?.statusCode || 500

    // 401/403 do not show error toast (handled by token refresh logic)
    if (status === 401 || status === 403) {
        return
    }

    // Extract error message
    const errorData: ApiErrorResponse | undefined = error?.response?._data || error?.data
    const errorMessage = errorData?.message || error?.message || 'Request failed, please try again later'

    // Choose toast type based on status code
    if (status >= 500) {
        message.error(errorMessage)
    } else if (status >= 400) {
        message.warning(errorMessage)
    } else {
        message.error(errorMessage)
    }
}

/**
 * useFetch wrapper with authentication
 * Automatically adds auth header, attempts to refresh token on 401/403
 */
export function useApi<T = any>(
    url: string | (() => string | null),
    options?: Parameters<typeof useFetch<T>>[1]
) {
    // Use computed to get the latest token on client
    const token = computed(() => getAccessToken())

    /**
     * Uniformly converge request to:
     * () => NitroFetchRequest | null
     *
     * ⚠️ Nuxt runtime supports null, but type is not declared
     *    Assertion only exists in encapsulation layer, business layer is completely safe
     */
    const request = (() => {
        return typeof url === 'function' ? url() : url
    }) as () => NitroFetchRequest


    return useFetch<T>(request, {
        cache: 'no-store',

        // Explicitly only request on client
        server: false,

        // Avoid duplicate concurrent requests
        dedupe: 'defer',

        ...options,
        // Skip request when URL is null
        headers: computed(() => {
            const baseHeaders =
                (unref(options?.headers) as Record<string, string> | undefined) ?? {}

            const headers: Record<string, string> = {
                ...baseHeaders,
            }
            if (token.value) {
                headers.Authorization = `Bearer ${token.value}`
            }
            return headers
        }),
        async onResponseError(ctx) {
            const { response } = ctx
            // Handle 401 (not authenticated) or 403 (no permission due to token expiration)
            if (response.status === 401 || response.status === 403) {
                console.warn(`[API] ${response.status}, attempting to refresh Token`)

                try {
                    // Attempt to refresh token
                    await refreshAccessToken()
                    console.info('[API] Token refreshed successfully, please retry request')
                } catch (error) {
                    // refreshAccessToken has already handled redirect/logout
                    console.error('[API] Token refresh failed')
                }
            } else {
                // Other errors: unified toast
                displayErrorToast(ctx)
            }
        },
    })
}

/**
 * $fetch wrapper with authentication
 * Automatically adds auth header, auto refreshes token and retries on 401/403
 */
export async function $api<T>(url: string, options?: Parameters<typeof $fetch>[1]): Promise<T> {
    const token = getAccessToken()

    try {
        return await $fetch<T>(url, {
            ...options,
            headers: {
                ...options?.headers,
                ...(token ? {Authorization: `Bearer ${token}`} : {}),
            },
        })
    } catch (error: any) {
        // Check if it's 401 (not authenticated) or 403 (no permission due to token expiration) error
        const status = error?.response?.status || error?.statusCode
        if (status === 401 || status === 403) {
            console.log(`[API] Received ${status} response, attempting to refresh token...`)

            try {
                // Attempt to refresh token
                const newToken = await refreshAccessToken()

                // Retry request with new token
                console.log('[API] Token refreshed successfully, retrying request...')
                return await $fetch<T>(url, {
                    ...options,
                    headers: {
                        ...options?.headers,
                        Authorization: `Bearer ${newToken}`,
                    },
                })
            } catch (refreshError) {
                // Refresh failed, already handled redirect in tokenRefresh
                console.error('[API] Token refresh failed, aborting request')
                throw refreshError
            }
        }

        // Other errors show toast and throw
        displayErrorToast(error)
        throw error
    }
}
