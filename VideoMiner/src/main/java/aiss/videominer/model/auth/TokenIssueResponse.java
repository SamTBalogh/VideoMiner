package aiss.videominer.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class TokenIssueResponse {

    @JsonProperty("tokenId")
    private String tokenId;

    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("tokenType")
    private String tokenType;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("expiresAt")
    private Instant expiresAt;

    public TokenIssueResponse() {
    }

    public TokenIssueResponse(String tokenId, String accessToken, String tokenType, Instant createdAt, Instant expiresAt) {
        this.tokenId = tokenId;
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
