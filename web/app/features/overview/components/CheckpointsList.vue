<template>
  <UCard>
    <template #header>
      <h3 class="text-lg font-semibold">Recent Checkpoints</h3>
    </template>

    <!-- Empty state -->
    <div v-if="isEmpty" class="text-center py-16 text-gray-500 dark:text-gray-400">
      <UIcon name="i-lucide-git-commit" class="w-12 h-12 mx-auto mb-4 opacity-50" />
      <p>No checkpoints in this range</p>
    </div>

    <!-- Table + Pagination -->
    <div v-else class="space-y-4">
      <UTable :data="listData" :columns="columns" class="flex-1" />

      <div class="flex justify-between items-center pt-4 border-t border-gray-200 dark:border-gray-700">
        <div class="text-sm text-default">Total {{ total }} items</div>
        <div class="flex items-center gap-3">
          <UPagination
            v-model:page="currentPage"
            :items-per-page="currentSize"
            :total="total"
            :max="7"
          />
          <USelect v-model="currentSize" :items="pageSizeOptions" class="w-36" />
        </div>
      </div>
    </div>
  </UCard>
</template>

<script setup lang="tsx">
import type { Row } from '@tanstack/vue-table'
import type { OverviewCheckpointListItem } from '~/shared/types/stats'
import type { PagerPayload } from '~/shared/types/api'
import { formatTokenCount } from '~/shared/utils/format'
import { useAgentColors } from '../composables/useAgentColors'

const { getAgentBadgeStyle } = useAgentColors()

function formatCommitMessage(msg?: string | null): string {
  if (!msg) return '-'
  return msg.split('\n')[0]
}

/** Format Unix ms to local datetime string (YYYY-MM-DD HH:mm) */
function formatCommitTime(ts?: number | null): string {
  if (ts == null) return '-'
  const d = new Date(ts)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

const props = defineProps<{
  listData: OverviewCheckpointListItem[]
  pager: PagerPayload<OverviewCheckpointListItem> | null
  pageSizeOptions: { label: string; value: number }[]
}>()

const emit = defineEmits<{
  pageChange: [page: number]
  updateSize: [size: number]
}>()

const isEmpty = computed(() => !props.listData?.length)
const total = computed(() => props.pager?.total ?? 0)

const currentPage = computed({
  get: () => props.pager?.page ?? 1,
  set: (v) => emit('pageChange', v),
})

const currentSize = computed({
  get: () => props.pager?.size ?? 20,
  set: (v) => emit('updateSize', v),
})

const columns = [
  {
    accessorKey: 'commitMessage',
    header: 'Commit Message',
    cell: ({ row }: { row: Row<OverviewCheckpointListItem> }) => (
      <div class="max-w-[280px]" title={row.original.commitMessage}>
        <span class="truncate block text-default">{formatCommitMessage(row.original.commitMessage)}</span>
      </div>
    ),
  },
  {
    accessorKey: 'commitAuthorName',
    header: 'Author',
    cell: ({ row }: { row: Row<OverviewCheckpointListItem> }) => (
      <div class="text-default">{row.original.commitAuthorName || '-'}</div>
    ),
  },
  {
    accessorKey: 'commitTime',
    header: 'Commit Time',
    cell: ({ row }: { row: Row<OverviewCheckpointListItem> }) => (
      <div class="whitespace-nowrap text-default">{formatCommitTime(row.original.commitTime)}</div>
    ),
  },
  {
    accessorKey: 'repoName',
    header: 'Repository',
    cell: ({ row }: { row: Row<OverviewCheckpointListItem> }) => (
      <div class="text-default">{row.original.repoName || '-'}</div>
    ),
  },
  {
    accessorKey: 'branch',
    header: 'Branch',
    cell: ({ row }: { row: Row<OverviewCheckpointListItem> }) => (
      <div class="text-default">{row.original.branch || '-'}</div>
    ),
  },
  {
    accessorKey: 'agent',
    header: 'Agent',
    cell: ({ row }: { row: Row<OverviewCheckpointListItem> }) =>
      row.original.agent ? (
        <UBadge
          size="xs"
          variant="soft"
          ui={{ base: 'border-0', color: { soft: '' } }}
          style={getAgentBadgeStyle(row.original.agent)}
        >
          {row.original.agent}
        </UBadge>
      ) : (
        <span class="text-gray-400">-</span>
      ),
  },
  {
    accessorKey: 'filesTouched',
    header: 'Files',
    cell: ({ row }: { row: Row<OverviewCheckpointListItem> }) => (
      <div class="text-default">{row.original.filesTouched ?? '-'}</div>
    ),
  },
  {
    accessorKey: 'tokenUsage',
    header: 'Tokens',
    cell: ({ row }: { row: Row<OverviewCheckpointListItem> }) => (
      <div class="text-default">{formatTokenCount(row.original.tokenUsage)}</div>
    ),
  },
  {
    accessorKey: 'additions',
    header: 'Additions / Deletions',
    cell: ({ row }: { row: Row<OverviewCheckpointListItem> }) => (
      <div class="text-default">
        <span class="text-green-600 dark:text-green-500">+{row.original.additions ?? 0}</span>
        <span class="text-gray-400 mx-1">/</span>
        <span class="text-red-600 dark:text-red-500">-{row.original.deletions ?? 0}</span>
      </div>
    ),
  },
]
</script>
