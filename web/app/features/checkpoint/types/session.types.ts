/**
 * Session module - types
 */

export interface SessionDTO {
  id: number
  checkpointId: number
  sessionId: string
  sessionIndex: number
  strategy?: string
  sessionCreatedAt?: number
  branch?: string
  checkpointsCount?: number
  filesTouchedCount?: number
  filesTouchedJson?: string
  agent?: string
  inputTokens?: number
  outputTokens?: number
  apiCallCount?: number
  agentLines?: number
  humanAdded?: number
  humanModified?: number
  humanRemoved?: number
  totalCommitted?: number
  agentPercentage?: number
  promptPreview?: string
  createdAt?: number
  updatedAt?: number
}
