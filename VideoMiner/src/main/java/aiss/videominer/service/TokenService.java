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
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class TokenService {

    public static final String MANAGEMENT_HEADER = "X-Token-Management-Key";
    public static final String BEARER_PREFIX = "Bearer ";
    private static final int TOKEN_BYTES_LENGTH = 32;
    private static final int TOKEN_GENERATION_MAX_ATTEMPTS = 5;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    TokenAuthProperties tokenAuthProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    public void validateConfiguration() {
        long minTtlHours = tokenAuthProperties.getMinTtlHours();
        long maxTtlHours = tokenAuthProperties.getMaxTtlHours();
        long defaultTtlHours = tokenAuthProperties.getDefaultTtlHours();

        if (minTtlHours > maxTtlHours) {
            throw new IllegalStateException("Token TTL configuration is invalid: min TTL cannot be greater than max TTL");
        }
        if (defaultTtlHours < minTtlHours || defaultTtlHours > maxTtlHours) {
            throw new IllegalStateException("Token TTL configuration is invalid: default TTL must be between min and max");
        }
    }

    @Transactional(readOnly = true)
    public void validate(HttpHeaders header) throws TokenRequiredException, TokenNotValidException {
        String tokenValue = extractBearerToken(header);
        String tokenHash = hashToken(tokenValue);

        Optional<Token> tokenOptional = tokenRepository.findByTokenHash(tokenHash);
        if (tokenOptional.isEmpty()) {
            throw new TokenNotValidException();
        }

        Token token = tokenOptional.get();
        if (!token.isActiveAt(Instant.now())) {
            throw new TokenNotValidException();
        }
    }

    @Transactional
    public TokenIssueResponse issueToken(String managementKey, TokenIssueRequest request)
            throws TokenManagementForbiddenException, TokenTtlOutOfRangeException {
        assertManagementKey(managementKey);

        long ttlHours = resolveTtlHours(request == null ? null : request.getTtlHours());
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(ttlHours, ChronoUnit.HOURS);

        for (int attempt = 0; attempt < TOKEN_GENERATION_MAX_ATTEMPTS; attempt++) {
            String plainToken = generateOpaqueToken();
            String tokenHash = hashToken(plainToken);

            if (tokenRepository.existsByTokenHash(tokenHash)) {
                continue;
            }

            Token token = new Token();
            token.setTokenHash(tokenHash);
            token.setCreatedAt(issuedAt);
            token.setExpiresAt(expiresAt);
            token.setRevoked(false);
            token.setRevokedAt(null);

            try {
                // Flush inside the retry loop so unique-constraint collisions are caught here.
                Token saved = tokenRepository.saveAndFlush(token);
                return new TokenIssueResponse(
                        saved.getId(),
                        plainToken,
                        "Bearer",
                        issuedAt,
                        expiresAt
                );
            } catch (DataIntegrityViolationException ignored) {
                // Retry with a fresh random token if the hash collides.
            }
        }

        throw new IllegalStateException("Unable to generate a unique token");
    }

    @Transactional
    public void revokeToken(String tokenId, String managementKey)
            throws TokenManagementForbiddenException, TokenNotFoundException {
        assertManagementKey(managementKey);

        Token token = tokenRepository.findById(tokenId).orElseThrow(TokenNotFoundException::new);
        if (token.isRevoked()) {
            return;
        }

        token.setRevoked(true);
        token.setRevokedAt(Instant.now());
        tokenRepository.save(token);
    }

    private void assertManagementKey(String managementKey) throws TokenManagementForbiddenException {
        if (managementKey == null || managementKey.isBlank()) {
            throw new TokenManagementForbiddenException();
        }

        byte[] provided = managementKey.getBytes(StandardCharsets.UTF_8);
        byte[] expected = tokenAuthProperties.getManagementKey().getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(provided, expected)) {
            throw new TokenManagementForbiddenException();
        }
    }

    private long resolveTtlHours(Long requestedTtlHours) throws TokenTtlOutOfRangeException {
        long ttlHours = requestedTtlHours == null
                ? tokenAuthProperties.getDefaultTtlHours()
                : requestedTtlHours;

        if (ttlHours < tokenAuthProperties.getMinTtlHours() || ttlHours > tokenAuthProperties.getMaxTtlHours()) {
            throw new TokenTtlOutOfRangeException();
        }
        return ttlHours;
    }

    private String extractBearerToken(HttpHeaders header) throws TokenRequiredException, TokenNotValidException {
        String authorization = header.getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            throw new TokenRequiredException();
        }

        if (!authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw new TokenNotValidException();
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new TokenNotValidException();
        }
        return token;
    }

    private String generateOpaqueToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashToken(String tokenValue) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(tokenAuthProperties.getPepper().getBytes(StandardCharsets.UTF_8));
            digest.update((byte) ':');
            return HexFormat.of().formatHex(digest.digest(tokenValue.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
