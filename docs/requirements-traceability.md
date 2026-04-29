# Requirements Traceability

This file maps MVP requirements to the current code foundation. It should be updated whenever a feature changes.

| Requirement | Current implementation | Primary code areas | Test coverage |
| --- | --- | --- | --- |
| Authentication and RBAC foundation | Registration, login, current-user endpoint, BCrypt password hashing, JWT generation/validation, role-protected routes | `AuthController`, `AuthenticationServiceImpl`, `JwtService`, `JwtAuthenticationFilter`, `UltronUserDetailsService`, `SecurityConfig`, `User` | `AuthenticationServiceImplTest`, `AuthSecurityIntegrationTest` |
| Athlete profile management | Create, retrieve, and search athlete profiles | `AthleteProfileController`, `AthleteProfileServiceImpl`, `AthleteProfile` | `AthleteProfileServiceImplTest`, `AthleteProfileRepositoryTest` |
| Coach/profile verification support | Coach profile creation with certification reference and verification status | `CoachProfileController`, `CoachProfileServiceImpl`, `CoachProfile` | `CoachProfileServiceImplTest` |
| Organisation/school/club records | Organisation creation and retrieval | `OrganisationController`, `OrganisationServiceImpl`, `Organisation` | `OrganisationServiceImplTest` |
| Evidence upload or evidence-link submission | Evidence submission stores sport, position, event type, date, match/training context, file URL/link, status, uploader, athlete profile, and AI status | `EvidenceUploadController`, `EvidenceUploadServiceImpl`, `EvidenceUpload` | `EvidenceUploadServiceImplTest` |
| Evidence verification workflow | Verification requests can be created, approved, rejected, and flagged | `VerificationRequestController`, `VerificationRequestServiceImpl`, `VerificationRequest` | `VerificationRequestServiceImplTest` |
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
- File upload scanning: `EvidenceUploadController`
- Rate limiting: `SecurityConfig` or API gateway
- POPIA/privacy compliance: `SecurityConfig`, future privacy services
- Admin moderation queues: `AdminModerationController`
