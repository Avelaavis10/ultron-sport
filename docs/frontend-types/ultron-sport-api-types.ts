// Ultron Sport MVP API reference types.
// This file is documentation, not production frontend code.
// Copy, refine, or regenerate these types inside a future frontend project.

export type UserRole =
  | "ATHLETE"
  | "COACH"
  | "ORGANISATION"
  | "SCOUT_AGENT"
  | "ADMIN";

export type AccountStatus = "PENDING" | "ACTIVE" | "SUSPENDED" | "DELETED";

export type VerificationStatus =
  | "DRAFT"
  | "SUBMITTED"
  | "PENDING_VERIFICATION"
  | "VERIFIED"
  | "REJECTED"
  | "FLAGGED"
  | "ARCHIVED";

export type EvidenceContext = "MATCH" | "TRAINING";

export type AiAnalysisStatus =
  | "NOT_STARTED"
  | "QUEUED"
  | "PROCESSING"
  | "COMPLETED"
  | "FAILED";

export type LevelPlayTier = "BRONZE" | "SILVER" | "GOLD" | "ELITE";

export type NotificationStatus = "UNREAD" | "READ";

export type NotificationType =
  | "EVIDENCE_SUBMITTED"
  | "EVIDENCE_VERIFIED"
  | "EVIDENCE_REJECTED"
  | "EVIDENCE_FLAGGED"
  | "EVIDENCE_ARCHIVED"
  | "LEVELPLAY_SCORE_CHANGED"
  | "ACHIEVEMENT_CREATED"
  | "ATHLETE_PROFILE_UPDATED"
  | "ORGANISATION_LINKED"
  | "COACH_PROFILE_UPDATED"
  | "SYSTEM";

export type NotificationTargetType =
  | "EVIDENCE"
  | "ATHLETE_PROFILE"
  | "ACHIEVEMENT"
  | "LEVELPLAY_SCORE"
  | "ORGANISATION"
  | "COACH_PROFILE"
  | "SYSTEM";

export type MediaStorageProvider = "LOCAL" | "MOCK" | "S3_TODO" | "AZURE_BLOB_TODO";

export type MediaUploadStatus = "UPLOADED" | "FAILED" | "LINK_ONLY";

export type MediaScanStatus =
  | "NOT_SCANNED"
  | "PENDING"
  | "PASSED"
  | "FAILED"
  | "SKIPPED_FOR_MVP";

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  code: string;
  traceId: string;
  validationErrors: Record<string, string>;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  sortBy: string;
  sortDirection: "ASC" | "DESC" | string;
}

export interface RegisterRequest {
  displayName: string;
  email: string;
  phone?: string | null;
  password: string;
  role: UserRole;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  tokenType: "Bearer" | string;
  accessToken: string;
  userId: number;
  displayName: string;
  email: string;
  role: UserRole;
}

export interface CurrentUserResponse {
  id: number;
  displayName: string;
  email: string;
  role: UserRole;
  status: AccountStatus;
}

export interface CreateAthleteProfileRequest {
  sport: string;
  position: string;
  age: number;
  gender?: string | null;
  location: string;
  schoolOrClub?: string | null;
  organisationId?: number | null;
  bio?: string | null;
}

export type UpdateAthleteProfileRequest = CreateAthleteProfileRequest;

export interface LinkAthleteOrganisationRequest {
  organisationId?: number | null;
  schoolOrClub?: string | null;
}

export interface AthleteProfileResponse {
  id: number;
  userId: number;
  sport: string;
  position: string;
  age: number;
  gender?: string | null;
  location: string;
  schoolOrClub?: string | null;
  organisationId?: number | null;
  bio?: string | null;
  profileCompletenessScore?: number | null;
  verificationStatus: VerificationStatus;
}

export interface CreateAchievementRequest {
  athleteProfileId: number;
  title: string;
  description?: string | null;
  achievedAt?: string | null;
}

export interface UpdateAchievementRequest {
  title: string;
  description?: string | null;
  achievedAt?: string | null;
}

export interface AchievementResponse {
  id: number;
  athleteProfileId: number;
  title: string;
  description?: string | null;
  achievedAt?: string | null;
  verified: boolean;
}

export interface CreateOrganisationRequest {
  name: string;
  type: string;
  location: string;
  contactEmail?: string | null;
  primaryAdminUserId?: number | null;
}

export interface OrganisationResponse {
  id: number;
  name: string;
  type: string;
  location: string;
  contactEmail?: string | null;
  primaryAdminUserId?: number | null;
  verificationStatus: VerificationStatus;
}

export interface CreateCoachProfileRequest {
  certificationReference: string;
  organisationId?: number | null;
  organisationName?: string | null;
  sport?: string | null;
  qualificationSummary?: string | null;
  yearsExperience?: number | null;
}

export type UpdateCoachProfileRequest = Partial<CreateCoachProfileRequest>;

export interface CoachProfileResponse {
  id: number;
  userId: number;
  certificationReference: string;
  organisationId?: number | null;
  organisationName?: string | null;
  sport?: string | null;
  qualificationSummary?: string | null;
  yearsExperience?: number | null;
  verificationStatus: VerificationStatus;
}

export interface CreateEvidenceRequest {
  athleteProfileId: number;
  title: string;
  description?: string | null;
  sport: string;
  position: string;
  eventType: string;
  matchOrTraining: EvidenceContext;
  eventDate: string;
  fileUrl?: string | null;
  externalVideoLink?: string | null;
}

export type UpdateEvidenceRequest = CreateEvidenceRequest;

export interface EvidenceResponse {
  id: number;
  athleteProfileId: number;
  uploadedByUserId: number;
  title: string;
  description?: string | null;
  sport: string;
  position: string;
  eventType: string;
  matchOrTraining: EvidenceContext;
  eventDate: string;
  fileUrl?: string | null;
  externalVideoLink?: string | null;
  mediaAssetId?: number | null;
  verificationStatus: VerificationStatus;
  aiAnalysisStatus: AiAnalysisStatus;
  createdAt: string;
  updatedAt: string;
}

export interface RejectEvidenceRequest {
  reason: string;
}

export interface FlagEvidenceRequest {
  reason: string;
}

export interface VerificationActionResponse {
  evidenceId: number;
  verificationStatus: VerificationStatus;
  message: string;
  changedAt: string;
}

export interface VerificationContextResponse {
  evidenceId: number;
  evidenceStatus: VerificationStatus;
  athleteProfileId: number;
  athleteUserId: number;
  athleteOrganisationId?: number | null;
  athleteOrganisationName?: string | null;
  athleteSchoolOrClub?: string | null;
  coachUserId?: number | null;
  coachProfileId?: number | null;
  coachOrganisationId?: number | null;
  coachOrganisationName?: string | null;
  sharedOrganisationContext?: boolean | null;
  latestVerificationRequestId?: number | null;
  latestVerificationStatus?: VerificationStatus | null;
  latestVerificationComments?: string | null;
  latestVerificationDecidedAt?: string | null;
  mvpWarning?: string | null;
}

export interface MediaAssetResponse {
  id: number;
  ownerUserId: number;
  athleteProfileId: number;
  evidenceUploadId?: number | null;
  originalFilename: string;
  contentType: string;
  fileSizeBytes: number;
  checksumSha256: string;
  storageProvider: MediaStorageProvider;
  publicUrl: string;
  uploadStatus: MediaUploadStatus;
  scanStatus: MediaScanStatus;
  createdAt: string;
  updatedAt: string;
}

export interface UploadMediaResponse {
  mediaId: number;
  publicUrl: string;
  media: MediaAssetResponse;
}

export interface AthleteDiscoveryCardResponse {
  athleteProfileId: number;
  displayName: string;
  sport: string;
  position: string;
  location: string;
  organisationName?: string | null;
  verifiedEvidenceCount: number;
  achievementCount: number;
  latestVerifiedEvidenceTitle?: string | null;
  levelPlayScore?: number | null;
  levelPlayTier?: string | null;
  profileCompletenessScore?: number | null;
}

export interface EvidenceDiscoveryCardResponse {
  evidenceId: number;
  athleteProfileId: number;
  athleteDisplayName: string;
  title: string;
  sport: string;
  position: string;
  eventType: string;
  matchOrTraining: EvidenceContext;
  eventDate: string;
  mediaUrl?: string | null;
  verificationStatus: VerificationStatus;
  aiAnalysisStatus: AiAnalysisStatus;
  createdAt: string;
  updatedAt: string;
}

export interface LevelPlayScoreResponse {
  id: number;
  athleteProfileId: number;
  verifiedEvidenceCount: number;
  coachVerificationCount: number;
  achievementCount: number;
  profileCompletenessScore: number;
  evidenceScore: number;
  achievementScore: number;
  verificationScore: number;
  profileCompletenessContribution: number;
  engagementScore: number;
  finalCredibilityScore: number;
  tier: LevelPlayTier;
  calculatedAt: string;
}

export interface LevelPlayScoreExplanationResponse {
  athleteProfileId: number;
  verifiedEvidenceCount: number;
  verifiedEvidenceCountScore: number;
  achievementCount: number;
  achievementScore: number;
  coachVerificationCount: number;
  coachVerificationScore: number;
  profileCompletenessScore: number;
  profileCompletenessContribution: number;
  finalCredibilityScore: number;
  tier: LevelPlayTier;
  explanationText: string;
  calculatedAt: string;
}

export interface NotificationResponse {
  id: number;
  recipientUserId: number;
  type: NotificationType;
  title: string;
  message: string;
  status: NotificationStatus;
  targetType: NotificationTargetType;
  targetId?: number | null;
  createdAt: string;
  readAt?: string | null;
}

export interface NotificationUnreadCountResponse {
  unreadCount: number;
}

export interface AdminActionLogResponse {
  id: number;
  adminUserId: number;
  adminEmail?: string | null;
  adminDisplayName?: string | null;
  actionType: string;
  targetType: string;
  targetId: number;
  reason?: string | null;
  details?: string | null;
  createdAt: string;
}

export interface ModerationSummaryResponse {
  flaggedEvidenceCount: number;
  archivedEvidenceCount: number;
  pendingVerificationCount: number;
  verifiedEvidenceCount: number;
  rejectedEvidenceCount: number;
}
