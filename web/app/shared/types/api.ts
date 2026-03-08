/**
 * Shared API type definitions
 */

export interface PagerPayload<T> {
  /**
   * Data list
   */
  data: T[]
  /**
   * Current page number
   */
  page: number
  /**
   * Number of items per page
   */
  size: number
  /**
   * Total number of items
   */
  total: number
}

/**
 * Status result interface
 */
export interface StatusResult<T = any> {
  code: string
  data: T
  message: string | null
}

////////////////////////////////

/**
 * Page result
 */
export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

/**
 * List query params
 */
export interface ListParams {
  page?: number
  pageSize?: number
  keyword?: string
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

/**
 * API response wrapper
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/**
 * API error response
 */
export interface ApiError {
  code: number
  message: string
  details?: any
}

/**
 * Backend standard error response format
 */
export interface ApiErrorResponse {
  status: number
  code: string
  message: string
  timestamp: number
}
