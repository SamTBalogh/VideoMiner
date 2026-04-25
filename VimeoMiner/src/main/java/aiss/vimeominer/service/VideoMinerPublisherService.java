package aiss.vimeominer.service;

import aiss.vimeominer.exception.ForbiddenException;
import aiss.vimeominer.model.VideoMiner.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VideoMinerPublisherService {

    private static final String MISSING_TOKEN_MESSAGE = "Authorization header with a valid Bearer token is required to publish to VideoMiner";
    private static final Pattern BEARER_PATTERN = Pattern.compile("^bearer\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    @Value("${videoMiner.url}")
    private String videoMinerUrl;

    @Autowired
    private RestTemplate restTemplate;

    public void publish(Channel channel, String token) throws ForbiddenException {
        String normalizedToken = normalizeBearerToken(token == null ? "" : token);
        if (normalizedToken.isBlank()) {
            throw new ForbiddenException(MISSING_TOKEN_MESSAGE);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", normalizedToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Channel> requestEntity = new HttpEntity<>(channel, headers);
            restTemplate.exchange(videoMinerUrl + "/channels", HttpMethod.POST, requestEntity, Void.class);
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseVideo(e.getMessage()));
        }
    }

    private String normalizeBearerToken(String token) {
        String trimmed = token.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (trimmed.equalsIgnoreCase("Bearer")) {
            return "";
        }
        Matcher bearerMatcher = BEARER_PATTERN.matcher(trimmed);
        if (bearerMatcher.matches()) {
            String value = bearerMatcher.group(1).trim();
            return value.isEmpty() ? "" : "Bearer " + value;
        }
        return "Bearer " + trimmed;
    }
}
