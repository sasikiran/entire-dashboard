import type { DateRangeOption } from '~/shared/types/stats'

export const DATE_RANGE_OPTIONS: DateRangeOption[] = [
  { label: 'Last 7 days', value: 'week' },
  { label: 'Last 14 days', value: 'twoWeeks' },
  { label: 'Last 30 days', value: 'month' },
  { label: 'Custom', value: 'custom' },
]
