package aiss.videominer.service;

import aiss.videominer.exception.TokenNotValidException;
import aiss.videominer.exception.TokenRequiredException;
import aiss.videominer.repository.TokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    TokenRepository tokenRepository;

    @InjectMocks
    TokenService tokenService;

    @Test
    void validate_missingAuthorizationHeader_throwsTokenRequiredException() {
        HttpHeaders headers = new HttpHeaders();

        assertThatThrownBy(() -> tokenService.validate(headers))
                .isInstanceOf(TokenRequiredException.class);
    }

    @Test
    void validate_invalidToken_throwsTokenNotValidException() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "invalid-token");
        when(tokenRepository.existsById("invalid-token")).thenReturn(false);

        assertThatThrownBy(() -> tokenService.validate(headers))
                .isInstanceOf(TokenNotValidException.class);
    }

    @Test
    void validate_validToken_doesNotThrow() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "valid-token");
        when(tokenRepository.existsById("valid-token")).thenReturn(true);

        assertThatCode(() -> tokenService.validate(headers)).doesNotThrowAnyException();
    }
}
