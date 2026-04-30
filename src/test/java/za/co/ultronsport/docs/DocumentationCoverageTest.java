package za.co.ultronsport.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
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
}
