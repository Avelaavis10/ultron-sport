package za.co.ultronsport.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentationCoverageTest {

    @Test
    void apiDocsMentionCurrentEndpointGroups() throws Exception {
        String docs = Files.readString(Path.of("docs", "api-endpoints-draft.md"));

        assertThat(docs)
                .contains("Authentication")
                .contains("Athlete Profiles")
                .contains("Achievements")
                .contains("Evidence")
                .contains("Media")
                .contains("Discovery")
                .contains("LevelPlay")
                .contains("Organisations")
                .contains("Coach Profiles")
                .contains("Admin Moderation")
                .contains("Notifications")
                .contains("Health");
    }

    @Test
    void handoverDocumentationFilesExist() {
        List<Path> requiredDocs = List.of(
                Path.of("docs", "api-endpoints-draft.md"),
                Path.of("docs", "api-testing-guide.md"),
                Path.of("docs", "local-development-guide.md"),
                Path.of("docs", "error-handling.md"),
                Path.of("docs", "health-checks.md"),
                Path.of("docs", "role-endpoint-access-matrix.md"),
                Path.of("docs", "manual-testing-seed-data.md"),
                Path.of("docs", "http", "ultron-sport-mvp.http")
        );

        requiredDocs.forEach(path -> assertThat(Files.exists(path))
                .as("Expected documentation file to exist: %s", path)
                .isTrue());
    }

    @Test
    void roleMatrixMentionsAllCurrentMvpRoles() throws Exception {
        String matrix = Files.readString(Path.of("docs", "role-endpoint-access-matrix.md"));

        assertThat(matrix)
                .contains("PUBLIC")
                .contains("ATHLETE")
                .contains("COACH")
                .contains("ORGANISATION")
                .contains("SCOUT_AGENT")
                .contains("ADMIN")
                .contains("VERIFIED evidence only")
                .contains("CoachProfile before verifying");
    }

    @Test
    void manualHttpCollectionCoversHappyPathAndNegativeTests() throws Exception {
        String collection = Files.readString(Path.of("docs", "http", "ultron-sport-mvp.http"));

        assertThat(collection)
                .contains("@baseUrl = http://localhost:8080")
                .contains("@adminToken =")
                .contains("@athleteToken =")
                .contains("@coachToken =")
                .contains("@organisationToken =")
                .contains("@scoutToken =")
                .contains("/api/auth/register")
                .contains("/api/organisations")
                .contains("/api/athlete-profiles")
                .contains("/api/coach-profiles")
                .contains("/api/achievements")
                .contains("/api/evidence")
                .contains("/api/discovery/athletes")
                .contains("/api/levelplay/athletes")
                .contains("/api/notifications")
                .contains("/api/admin/audit-logs")
                .contains("Negative 1")
                .contains("Negative 8");
    }

    @Test
    void apiContractDocumentsPrimaryEndpointGroupsAndLegacyNote() throws Exception {
        String docs = Files.readString(Path.of("docs", "api-endpoints-draft.md"));

        assertThat(docs)
                .contains("Primary API base path")
                .contains("/api/auth")
                .contains("/api/athlete-profiles")
                .contains("/api/achievements")
                .contains("/api/organisations")
                .contains("/api/coach-profiles")
                .contains("/api/evidence")
                .contains("/api/media")
                .contains("/api/discovery")
                .contains("/api/levelplay")
                .contains("/api/admin")
                .contains("/api/notifications")
                .contains("/api/health")
                .contains("Legacy compatibility");
    }
}
