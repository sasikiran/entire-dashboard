/**
 * Route guard: check if user is logged in
 */
import { getAccessToken } from '~/shared/utils/tokenStorage'
import { ROUTES } from '~/shared/constants/routes'

export default defineNuxtRouteMiddleware((to) => {
  if (!getAccessToken()) {
    return navigateTo(ROUTES.LOGIN)
  }
})