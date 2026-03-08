import type { PagerPayload } from './api'

/**
 * Project stats item
 */
export interface ProjectStatsItem {
  projectName: string
  commitCount: number
  averageScore: number
}

/**
 * Author stats item
 */
export interface AuthorStatsItem {
  author: string
  commitCount: number
  averageScore: number
}

/**
 * Code change stats item
 */
export interface CodeChangeStatsItem {
  name: string
  additions: number
  deletions: number
}

/**
 * Stats data type definitions
 */
export interface StatsData {
  activeProjects: number
  contributors: number
  totalCommits: number
  averageScore: number
  projectCommitStats: ProjectStatsItem[]
  projectAverageScoreStats: ProjectStatsItem[]
  authorCommitStats: AuthorStatsItem[]
  authorAverageScoreStats: AuthorStatsItem[]
  projectCodeChangeStats: CodeChangeStatsItem[]
  authorCodeChangeStats: CodeChangeStatsItem[]
}

/**
 * Date range type
 */
export type DateRangeType = 'week' | 'twoWeeks' | 'month' | 'custom'

/**
 * Date range option
 */
export interface DateRangeOption {
  label: string
  value: DateRangeType
}

/**
 * Overview statistics (from /api/v1/admin/overview/stats)
 */
export interface OverviewStats {
  activeProjectCount: number
  submitterCount: number
  checkpointCount: number
  totalTokenUsage: number
}

/**
 * Overview stats query params
 */
export interface OverviewStatsParams {
  startTime?: number
  endTime?: number
}

/**
 * Overview checkpoint chart item (for Contribution scatter plot)
 */
export interface OverviewCheckpointChartItem {
  checkpointId: string
  commitTime: number
  additions: number
  deletions: number
  agent: string
}

/**
 * Overview checkpoint list item
 */
export interface OverviewCheckpointListItem {
  id: number
  checkpointId: string
  commitMessage: string
  commitAuthorName?: string
  repoName: string
  branch: string
  agent: string
  filesTouched: number
  additions: number
  deletions: number
  commitTime: number
  tokenUsage?: number
}

/**
 * Overview agent stat (for chart legend)
 */
export interface OverviewAgentStat {
  agent: string
  count: number
  percentage: number
}

/**
 * Overview checkpoints response
 */
export interface OverviewCheckpointsResponse {
  chartData: OverviewCheckpointChartItem[]
  chartDataTruncated: boolean
  list: PagerPayload<OverviewCheckpointListItem>
  agentStats: OverviewAgentStat[]
}

/**
 * Overview checkpoints query params
 */
export interface OverviewCheckpointsParams {
  startTime?: number
  endTime?: number
  page?: number
  size?: number
}
