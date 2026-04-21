package aiss.videominer.service;

import aiss.videominer.exception.TokenNotValidException;
import aiss.videominer.exception.TokenRequiredException;
import aiss.videominer.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Autowired
    TokenRepository tokenRepository;

    public void validate(HttpHeaders header) throws TokenRequiredException, TokenNotValidException {
        String token = header.getFirst("Authorization");
        if (token == null) {
            throw new TokenRequiredException();
        }
        if (!tokenRepository.existsById(token)) {
            throw new TokenNotValidException();
        }
    }
}
