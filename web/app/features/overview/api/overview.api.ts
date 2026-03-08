/**
 * Overview module - API client
 */
import { $adminApi } from '~/api/admin-api-client'
import type { PagerPayload } from '~/shared/types/api'
import type {
  OverviewStats,
  OverviewStatsParams,
  OverviewCheckpointsResponse,
  OverviewCheckpointsParams,
  OverviewCheckpointListItem,
} from '~/shared/types/stats'

export const overviewApi = {
  /** Get overview statistics for the given time range */
  async getStats(params?: OverviewStatsParams) {
    return $adminApi<OverviewStats>('/overview/stats', {
      method: 'GET',
      query: params as Record<string, string | number | undefined>,
    })
  },

  /** Get checkpoints for Contribution chart and list (chart + first page list) */
  async getCheckpoints(params?: OverviewCheckpointsParams) {
    return $adminApi<OverviewCheckpointsResponse>('/overview/checkpoints', {
      method: 'GET',
      query: params as Record<string, string | number | undefined>,
    })
  },

  /** Get paginated checkpoint list only (for pagination, no chart data) */
  async getCheckpointsList(params?: OverviewCheckpointsParams) {
    return $adminApi<PagerPayload<OverviewCheckpointListItem>>('/overview/checkpoints/list', {
      method: 'GET',
      query: params as Record<string, string | number | undefined>,
    })
  },
}
