/**
 * Shared common type definitions
 */

/**
 * Base entity interface
 */
export interface BaseDTO {
  id: string
  createdAt: string
  updatedAt: string
}

/**
 * System info interface
 */
export interface SystemInfoDTO {
  version: string
  licenseLevel: string
  expireAt: number | null
  currentProjectCount: number
  projectMaxCount: number
  canCreateProject: boolean
  remainingProjectCount: number
  accessKey?: string | null
}

/**
 * Public system info interface
 */
export interface PublicSystemInfoDTO {
  siteName: string
  siteNotice: string
}

/**
 * Cloud version change item (same as changes element returned by /api/v1/admin/system/version/latest)
 */
export interface VersionChangeItemDTO {
  version: string
  featuresMd: string
}

/**
 * Cloud latest version API response
 */
export interface VersionLatestResponseDTO {
  currentVersion: string
  latestVersion: string
  changes: VersionChangeItemDTO[]
}

