package aiss.videominer.service;

import aiss.videominer.config.TokenAuthProperties;
import aiss.videominer.exception.TokenManagementForbiddenException;
import aiss.videominer.exception.TokenNotFoundException;
import aiss.videominer.exception.TokenNotValidException;
import aiss.videominer.exception.TokenRequiredException;
import aiss.videominer.exception.TokenTtlOutOfRangeException;
import aiss.videominer.model.Token;
import aiss.videominer.model.auth.TokenIssueRequest;
import aiss.videominer.model.auth.TokenIssueResponse;
import aiss.videominer.repository.TokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    TokenRepository tokenRepository;

    @Mock
    TokenAuthProperties tokenAuthProperties;

    @InjectMocks
    TokenService tokenService;

    private Token buildActiveToken() {
        Token token = new Token();
        token.setId("token-id");
        token.setTokenHash("hash");
        token.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        token.setRevoked(false);
        return token;
    }

    private void stubManagementKey() {
        when(tokenAuthProperties.getManagementKey()).thenReturn("test-management-key");
    }

    private void stubIssueTokenConfiguration() {
        when(tokenAuthProperties.getPepper()).thenReturn("test-pepper");
        when(tokenAuthProperties.getMinTtlHours()).thenReturn(1L);
        when(tokenAuthProperties.getMaxTtlHours()).thenReturn(720L);
    }

    @Test
    void validate_missingAuthorizationHeader_throwsTokenRequiredException() {
        HttpHeaders headers = new HttpHeaders();

        assertThatThrownBy(() -> tokenService.validate(headers))
                .isInstanceOf(TokenRequiredException.class);
    }

    @Test
    void validate_withoutBearerPrefix_throwsTokenNotValidException() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "invalid-token");

        assertThatThrownBy(() -> tokenService.validate(headers))
                .isInstanceOf(TokenNotValidException.class);
    }

    @Test
    void validate_unknownBearerToken_throwsTokenNotValidException() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer unknown");
        when(tokenAuthProperties.getPepper()).thenReturn("test-pepper");
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.validate(headers))
                .isInstanceOf(TokenNotValidException.class);
    }

    @Test
    void validate_expiredToken_throwsTokenNotValidException() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer expired");

        Token token = buildActiveToken();
        token.setExpiresAt(Instant.now().minusSeconds(60));

        when(tokenAuthProperties.getPepper()).thenReturn("test-pepper");
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> tokenService.validate(headers))
                .isInstanceOf(TokenNotValidException.class);
    }

    @Test
    void validate_revokedToken_throwsTokenNotValidException() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer revoked");

        Token token = buildActiveToken();
        token.setRevoked(true);

        when(tokenAuthProperties.getPepper()).thenReturn("test-pepper");
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> tokenService.validate(headers))
                .isInstanceOf(TokenNotValidException.class);
    }

    @Test
    void validate_validBearerToken_doesNotThrow() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer valid-token");

        when(tokenAuthProperties.getPepper()).thenReturn("test-pepper");
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(buildActiveToken()));

        assertThatCode(() -> tokenService.validate(headers)).doesNotThrowAnyException();
    }

    @Test
    void issueToken_validManagementKey_returnsPlainTokenAndStoresOnlyHash() throws Exception {
        stubManagementKey();
        stubIssueTokenConfiguration();

        TokenIssueRequest request = new TokenIssueRequest();
        request.setTtlHours(24L);

        doAnswer(invocation -> {
            Token token = invocation.getArgument(0);
            token.setId("generated-id");
            return token;
        }).when(tokenRepository).saveAndFlush(any(Token.class));

        TokenIssueResponse response = tokenService.issueToken("test-management-key", request);

        assertThat(response.getTokenId()).isEqualTo("generated-id");
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresAt()).isAfter(response.getCreatedAt());

        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).saveAndFlush(captor.capture());
        Token persisted = captor.getValue();

        assertThat(persisted.getTokenHash()).isNotBlank();
        assertThat(persisted.getTokenHash()).hasSize(64);
        assertThat(persisted.getTokenHash()).isNotEqualTo(response.getAccessToken());
    }

    @Test
    void issueToken_collisionOnFlush_retriesAndSucceeds() throws Exception {
        stubManagementKey();
        stubIssueTokenConfiguration();

        TokenIssueRequest request = new TokenIssueRequest();
        request.setTtlHours(24L);

        when(tokenRepository.saveAndFlush(any(Token.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"))
                .thenAnswer(invocation -> {
                    Token token = invocation.getArgument(0);
                    token.setId("generated-id-after-retry");
                    return token;
                });

        TokenIssueResponse response = tokenService.issueToken("test-management-key", request);

        assertThat(response.getTokenId()).isEqualTo("generated-id-after-retry");
        verify(tokenRepository, times(2)).saveAndFlush(any(Token.class));
    }

    @Test
    void issueToken_invalidManagementKey_throwsForbidden() {
        stubManagementKey();

        assertThatThrownBy(() -> tokenService.issueToken("wrong-key", new TokenIssueRequest()))
                .isInstanceOf(TokenManagementForbiddenException.class);
    }

    @Test
    void issueToken_ttlOutOfRange_throwsBadRequest() {
        stubManagementKey();
        when(tokenAuthProperties.getMinTtlHours()).thenReturn(1L);
        when(tokenAuthProperties.getMaxTtlHours()).thenReturn(720L);

        TokenIssueRequest request = new TokenIssueRequest();
        request.setTtlHours(9999L);

        assertThatThrownBy(() -> tokenService.issueToken("test-management-key", request))
                .isInstanceOf(TokenTtlOutOfRangeException.class);
    }

    @Test
    void revokeToken_existingToken_marksAsRevoked() throws Exception {
        stubManagementKey();

        Token token = buildActiveToken();
        token.setId("token-id");
        when(tokenRepository.findById("token-id")).thenReturn(Optional.of(token));
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tokenService.revokeToken("token-id", "test-management-key");

        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(captor.capture());
        Token revoked = captor.getValue();
        assertThat(revoked.isRevoked()).isTrue();
        assertThat(revoked.getRevokedAt()).isNotNull();
    }

    @Test
    void revokeToken_missingToken_throwsNotFound() {
        stubManagementKey();
        when(tokenRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.revokeToken("missing", "test-management-key"))
                .isInstanceOf(TokenNotFoundException.class);
    }
}
