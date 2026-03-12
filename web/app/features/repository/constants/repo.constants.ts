/**
 * Repository module - constants
 */

/** Platform type options */
export const PLATFORM_OPTIONS = [
  { label: 'GitHub', value: 'GITHUB' },
  { label: 'GitLab', value: 'GITLAB' },
  { label: 'Gitee', value: 'GITEE' },
  { label: 'Gitea', value: 'GITEA' },
  { label: 'Local', value: 'LOCAL' },
]

export const REMOTE_PLATFORM_OPTIONS = PLATFORM_OPTIONS.filter((item) => item.value !== 'LOCAL')
