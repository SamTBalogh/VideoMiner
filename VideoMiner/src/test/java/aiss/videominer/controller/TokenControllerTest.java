package aiss.videominer.controller;

import aiss.videominer.exception.TokenManagementForbiddenException;
import aiss.videominer.model.auth.TokenIssueResponse;
import aiss.videominer.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TokenController.class)
class TokenControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TokenService tokenService;

    // ---- POST /token ----

    @Test
    void issueToken_validRequest_returns201() throws Exception {
        TokenIssueResponse response = new TokenIssueResponse(
                "token-id-1",
                "plain-token-value",
                "Bearer",
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-02T10:00:00Z")
        );

        when(tokenService.issueToken(any(), any())).thenReturn(response);

        mockMvc.perform(post("/videoMiner/v1/token")
                        .header(TokenService.MANAGEMENT_HEADER, "management-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ttlHours\":24}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenId").value("token-id-1"))
                .andExpect(jsonPath("$.accessToken").value("plain-token-value"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void issueToken_withoutManagementHeader_returns403() throws Exception {
        doThrow(TokenManagementForbiddenException.class).when(tokenService).issueToken(any(), any());

        mockMvc.perform(post("/videoMiner/v1/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ttlHours\":24}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void issueToken_invalidBody_returns400() throws Exception {
        mockMvc.perform(post("/videoMiner/v1/token")
                        .header(TokenService.MANAGEMENT_HEADER, "management-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ttlHours\":0}"))
                .andExpect(status().isBadRequest());
    }

    // ---- DELETE /token/{id} ----

    @Test
    void revokeToken_existingToken_returns204() throws Exception {
        doNothing().when(tokenService).revokeToken(any(), any());

        mockMvc.perform(delete("/videoMiner/v1/token/token-id-1")
                        .header(TokenService.MANAGEMENT_HEADER, "management-secret"))
                .andExpect(status().isNoContent());
    }
}
