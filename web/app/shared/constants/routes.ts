/**
 * Route constant definitions
 */

export const ROUTES = {
  // Public routes
  HOME: '/',
  LOGIN: '/login',

  // Admin routes
  ADMIN: {
    INDEX: '/admin',
    OVERVIEW: '/admin/overview',
  },
} as const
