/**
 * Composable for fetching overview statistics with date range
 */
import { overviewApi } from '../api/overview.api'
import type { DateRangeType } from '~/shared/types/stats'
import { formatDate, getDaysAgo } from '~/shared/utils/date'

/** Get start of day 00:00:00 in local timezone as Unix ms */
function getStartOfDay(date: Date): number {
  const d = new Date(date)
  d.setHours(0, 0, 0, 0)
  return d.getTime()
}

/** Get end of day 23:59:59.999 in local timezone as Unix ms */
function getEndOfDay(date: Date): number {
  const d = new Date(date)
  d.setHours(23, 59, 59, 999)
  return d.getTime()
}

/** Compute startTime and endTime from date range type */
function getTimeRange(
  dateRangeType: DateRangeType,
  customStart?: Date,
  customEnd?: Date
): { startTime: number; endTime: number } {
  const today = new Date()
  const endTime = getEndOfDay(today)

  if (dateRangeType === 'custom' && customStart && customEnd) {
    return {
      startTime: getStartOfDay(customStart),
      endTime: getEndOfDay(customEnd),
    }
  }

  const daysAgo = dateRangeType === 'week' ? 7 : dateRangeType === 'twoWeeks' ? 14 : 30
  const startDate = new Date(today)
  startDate.setDate(startDate.getDate() - daysAgo)
  const startTime = getStartOfDay(startDate)

  return { startTime, endTime }
}

export function useOverviewStats() {
  const dateRangeType = ref<DateRangeType>('week')
  const customStartDate = ref<string>('')
  const customEndDate = ref<string>('')

  const queryParams = computed(() => {
    if (dateRangeType.value === 'custom' && customStartDate.value && customEndDate.value) {
      const start = new Date(customStartDate.value)
      const end = new Date(customEndDate.value)
      return getTimeRange('custom', start, end)
    }
    return getTimeRange(dateRangeType.value)
  })

  const statsData = ref<Awaited<ReturnType<typeof overviewApi.getStats>> | null>(null)
  const isLoading = ref(false)
  const fetchError = ref<Error | null>(null)

  async function fetchStats() {
    isLoading.value = true
    fetchError.value = null
    try {
      const params = queryParams.value
      statsData.value = await overviewApi.getStats({
        startTime: params.startTime,
        endTime: params.endTime,
      })
    } catch (e) {
      fetchError.value = e as Error
    } finally {
      isLoading.value = false
    }
  }

  watch(
    [dateRangeType, customStartDate, customEndDate],
    () => {
      if (dateRangeType.value === 'custom' && (!customStartDate.value || !customEndDate.value)) {
        statsData.value = null
        return
      }
      fetchStats()
    },
    { immediate: true }
  )

  function selectDateRange(range: DateRangeType) {
    dateRangeType.value = range
    if (range === 'custom') {
      const start = getDaysAgo(7)
      const end = new Date()
      customStartDate.value = formatDate(start)
      customEndDate.value = formatDate(end)
    }
  }

  return {
    stats: statsData,
    pending: isLoading,
    error: fetchError,
    refresh: fetchStats,
    dateRangeType,
    customStartDate,
    customEndDate,
    selectDateRange,
    queryParams,
  }
}
