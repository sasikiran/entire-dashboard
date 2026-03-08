<template>
  <div class="flex flex-wrap items-end gap-4">
    <UFormField label="Start Date">
      <UInput v-model="startDateStr" type="date" class="w-40" />
    </UFormField>
    <UFormField label="End Date">
      <UInput v-model="endDateStr" type="date" class="w-40" />
    </UFormField>
    <UFormField label="Repository">
      <USelectMenu
        v-model="repoIds"
        multiple
        value-key="repoId"
        :items="repoItems"
        placeholder="All Repositories"
        class="w-48"
        :loading="loadingRepos"
      />
    </UFormField>
    <UFormField label="Author">
      <USelectMenu
        v-model="commitAuthorNames"
        multiple
        value-key="value"
        :items="commitAuthorItems"
        placeholder="All Authors"
        class="w-48"
        :loading="loadingAuthors"
      />
    </UFormField>
    <UFormField label="Commit Message">
      <UInput
        v-model="commitMessage"
        placeholder="Search"
        class="w-56"
        :ui="{ trailing: 'pr-0.5' }"
      >
        <template v-if="commitMessage.length" #trailing>
          <UButton
            color="neutral"
            variant="link"
            size="sm"
            icon="i-lucide-circle-x"
            aria-label="Clear"
            @click="commitMessage = ''"
          />
        </template>
      </UInput>
    </UFormField>
    <UButton label="Reset" color="neutral" variant="outline" @click="$emit('reset')" />
  </div>
</template>

<script setup lang="ts">
import type { RepoOption } from '../types/checkpoint.types'

const props = defineProps<{
  startDateStr: string
  endDateStr: string
  repoIds: number[]
  commitAuthorNames: string[]
  commitMessage: string
  repoOptions: RepoOption[]
  commitAuthorOptions: string[]
  loadingRepos: boolean
  loadingAuthors: boolean
}>()

const emit = defineEmits<{
  'update:startDateStr': [value: string]
  'update:endDateStr': [value: string]
  'update:repoIds': [value: number[]]
  'update:commitAuthorNames': [value: string[]]
  'update:commitMessage': [value: string]
  reset: []
}>()

const startDateStr = computed({
  get: () => props.startDateStr,
  set: (v) => emit('update:startDateStr', v),
})
const endDateStr = computed({
  get: () => props.endDateStr,
  set: (v) => emit('update:endDateStr', v),
})
const repoIds = computed({
  get: () => props.repoIds,
  set: (v) => {
    const arr = Array.isArray(v) ? v : v ? [v] : []
    const ids = arr.map((x) => (typeof x === 'object' && x != null && 'repoId' in x ? (x as { repoId: number }).repoId : Number(x)))
    emit('update:repoIds', ids.filter((id) => !Number.isNaN(id)))
  },
})
const commitAuthorNames = computed({
  get: () => props.commitAuthorNames,
  set: (v) => {
    const arr = Array.isArray(v) ? v : v ? [v] : []
    const names = arr.map((x) => (typeof x === 'object' && x != null && 'value' in x ? (x as { value: string }).value : String(x)))
    emit('update:commitAuthorNames', names)
  },
})
const commitMessage = computed({
  get: () => props.commitMessage,
  set: (v) => emit('update:commitMessage', v),
})

const repoItems = computed(() =>
  props.repoOptions.map((r) => ({ label: r.repoName, repoId: r.repoId }))
)
const commitAuthorItems = computed(() =>
  props.commitAuthorOptions.map((name) => ({ label: name, value: name }))
)

</script>
