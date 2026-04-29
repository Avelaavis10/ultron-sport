package za.co.ultronsport.config.storage;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import za.co.ultronsport.domain.MediaStorageProvider;

@Component
@ConfigurationProperties(prefix = "storage")
public class MediaStorageProperties {

    private MediaStorageProvider mode = MediaStorageProvider.LOCAL;
    private Local local = new Local();
    private String publicBaseUrl = "http://localhost:8080/media";
    private long maxFileSizeBytes = 52_428_800L;
    private Set<String> supportedContentTypes = new LinkedHashSet<>(Set.of(
            "video/mp4",
            "video/quicktime",
            "image/jpeg",
            "image/png"
    ));

    public MediaStorageProvider getMode() {
        return mode;
    }

    public void setMode(MediaStorageProvider mode) {
        this.mode = mode;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public Set<String> getSupportedContentTypes() {
        return supportedContentTypes;
    }

    public void setSupportedContentTypes(Set<String> supportedContentTypes) {
        this.supportedContentTypes = supportedContentTypes;
    }

    public static class Local {
        private String basePath = "./uploads/ultron-sport";

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }
}
