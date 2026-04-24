package aiss.videominer.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;

public class TokenIssueRequest {

    @JsonProperty("ttlHours")
    @Min(1)
    private Long ttlHours;

    public Long getTtlHours() {
        return ttlHours;
    }

    public void setTtlHours(Long ttlHours) {
        this.ttlHours = ttlHours;
    }
}
