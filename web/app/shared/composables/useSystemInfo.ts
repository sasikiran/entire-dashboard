/**
 * System info Composable
 */

import type { SystemInfoDTO } from '~/shared/types/dtos'
import { useAdminApi } from '~/api/admin-api-client'

/**
 * Get system info
 */
export function useSystemInfo() {
  return useAdminApi<SystemInfoDTO>('/system/info', {
    method: 'GET',
  })
}
