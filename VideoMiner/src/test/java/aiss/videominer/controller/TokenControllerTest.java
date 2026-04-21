package aiss.videominer.controller;

import aiss.videominer.model.Token;
import aiss.videominer.repository.TokenRepository;
import aiss.videominer.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TokenController.class)
class TokenControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TokenRepository tokenRepository;

    // TokenService is not used by TokenController but may be in context
    @MockBean
    TokenService tokenService;

    // ---- POST /token ----

    @Test
    void addToken_validToken_returns201() throws Exception {
        Token token = new Token();
        token.setId("my-secret-token");

        when(tokenRepository.save(any(Token.class))).thenReturn(token);

        mockMvc.perform(post("/videoMiner/v1/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("my-secret-token"));
    }

    @Test
    void addToken_nullId_returns400() throws Exception {
        Token token = new Token(); // id is null

        mockMvc.perform(post("/videoMiner/v1/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(token)))
                .andExpect(status().isBadRequest());
    }
}
