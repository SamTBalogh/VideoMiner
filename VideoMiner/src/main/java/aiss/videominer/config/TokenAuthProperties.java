package aiss.videominer.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "videominer.auth.token")
public class TokenAuthProperties {

    @NotBlank
    private String pepper;

    @NotBlank
    private String managementKey;

    @Min(1)
    private long defaultTtlHours = 24;

    @Min(1)
    private long minTtlHours = 1;

    @Min(1)
    private long maxTtlHours = 720;

    public String getPepper() {
        return pepper;
    }

    public void setPepper(String pepper) {
        this.pepper = pepper;
    }

    public String getManagementKey() {
        return managementKey;
    }

    public void setManagementKey(String managementKey) {
        this.managementKey = managementKey;
    }

    public long getDefaultTtlHours() {
        return defaultTtlHours;
    }

    public void setDefaultTtlHours(long defaultTtlHours) {
        this.defaultTtlHours = defaultTtlHours;
    }

    public long getMinTtlHours() {
        return minTtlHours;
    }

    public void setMinTtlHours(long minTtlHours) {
        this.minTtlHours = minTtlHours;
    }

    public long getMaxTtlHours() {
        return maxTtlHours;
    }

    public void setMaxTtlHours(long maxTtlHours) {
        this.maxTtlHours = maxTtlHours;
    }
}
