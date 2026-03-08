/**
 * Checkpoint module - types
 */

export interface CheckpointDTO {
  id: number
  checkpointId: string
  repoId: number
  repoName?: string
  branch: string
  commitSha: string
  commitMessage: string
  commitAuthorName?: string
  commitTime: number
  checkpointsCount?: number
  filesTouched?: number
  additions?: number
  deletions?: number
  tokenUsage?: number
  agent?: string
  commitUrl?: string
  createdAt?: number
  updatedAt?: number
}

export interface RepoOption {
  repoId: number
  repoName: string
}

export interface CheckpointSearchParams {
  startTime?: number
  endTime?: number
  repoIds?: number[]
  commitAuthorNames?: string[]
  commitMessage?: string
}

export interface CheckpointFilterParams {
  startTime: number
  endTime: number
  repoIds?: number[]
  commitAuthorNames?: string[]
  commitMessage?: string
}
