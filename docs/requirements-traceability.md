# Requirements Traceability

This file maps MVP requirements to the current code foundation. It should be updated whenever a feature changes.

| Requirement | Current implementation | Primary code areas | Test coverage |
| --- | --- | --- | --- |
| Authentication and RBAC foundation | Registration, login, current-user endpoint, BCrypt password hashing, JWT generation/validation, role-protected routes | `AuthController`, `AuthenticationServiceImpl`, `JwtService`, `JwtAuthenticationFilter`, `UltronUserDetailsService`, `SecurityConfig`, `User` | `AuthenticationServiceImplTest`, `AuthSecurityIntegrationTest` |
| Athlete profile management | Create, retrieve, and search athlete profiles | `AthleteProfileController`, `AthleteProfileServiceImpl`, `AthleteProfile` | `AthleteProfileServiceImplTest`, `AthleteProfileRepositoryTest` |
| Coach/profile verification support | Coach profile creation with certification reference and verification status | `CoachProfileController`, `CoachProfileServiceImpl`, `CoachProfile` | `CoachProfileServiceImplTest` |
| Organisation/school/club records | Organisation creation and retrieval | `OrganisationController`, `OrganisationServiceImpl`, `Organisation` | `OrganisationServiceImplTest` |
| UR-03 / FR-03 Evidence upload | Athletes create DRAFT evidence with title, description, sport, position, event type, match/training indicator, event date, file URL or external video link, uploader, and athlete profile ownership | `EvidenceController`, `EvidenceServiceImpl`, `EvidenceUpload`, `CreateEvidenceRequest`, `UpdateEvidenceRequest` | `EvidenceServiceImplTest`, `EvidenceWorkflowIntegrationTest` |
| UR-04 / FR-04 Evidence verification | Athletes submit evidence to PENDING_VERIFICATION; coaches verify or reject with reason; admins flag or archive evidence | `EvidenceController`, `EvidenceServiceImpl`, `VerificationRequest`, `VerificationRequestRepository` | `EvidenceServiceImplTest`, `EvidenceWorkflowIntegrationTest`, `VerificationRequestServiceImplTest` |
| UR-05 / FR-05 AI-ready evidence metadata | Evidence stores structured metadata and `AiAnalysisStatus`, defaulting to NOT_STARTED without calling AI services | `EvidenceUpload`, `AiAnalysisStatus`, `EvidenceResponse` | `EvidenceServiceImplTest`, `EvidenceWorkflowIntegrationTest` |
| FR-07 Evidence discovery readiness | SCOUT_AGENT and ORGANISATION users can view VERIFIED evidence only; pending/draft visibility is restricted | `EvidenceServiceImpl`, `SecurityConfig` | `EvidenceWorkflowIntegrationTest` |
| Security/RBAC non-functional requirements | Evidence endpoints enforce ATHLETE ownership, COACH verification rights, ADMIN moderation rights, and clean 401/403 failures | `SecurityConfig`, `EvidenceController`, `EvidenceServiceImpl` | `EvidenceWorkflowIntegrationTest` |
| Athlete search and filtering | Search foundation supports sport, location, and position filters | `AthleteProfileController`, `AthleteProfileServiceImpl` | `AthleteProfileServiceImplTest` |
| Basic LevelPlay placeholder | Score stores verified evidence count, coach verification count, achievement count, profile completeness, engagement, final score, and tier | `LevelPlayScoreController`, `LevelPlayScoreServiceImpl`, `LevelPlayScore` | `LevelPlayScoreServiceImplTest` |
| Admin moderation foundation | Admin action log endpoint and entity | `AdminModerationController`, `AdminActionLogServiceImpl`, `AdminActionLog` | `AdminActionLogServiceImplTest` |
| Audit logging foundation | Admin action log exists; verification/admin TODOs remain | `AdminActionLog`, `AdminModerationController` | `AdminActionLogServiceImplTest` |
| Testing structure | Maven test setup with unit and repository integration tests | `src/test/java` | `mvn test` |

## Known TODO Traceability

- Refresh tokens: future auth service work
- Password reset: future auth service work
- Email/phone verification: `AuthenticationServiceImpl`
- Account lockout and MFA: future security service work
- OAuth/social login: future identity integration
- Audit logging expansion: `VerificationRequestController`, `AdminModerationController`
- File upload scanning: `EvidenceController`
- Object storage/CDN integration: future evidence storage service
- AI analysis job dispatch: future AI analysis service
- Rate limiting: `SecurityConfig` or API gateway
- POPIA/privacy compliance: `SecurityConfig`, future privacy services
- Admin moderation queues: `AdminModerationController`
