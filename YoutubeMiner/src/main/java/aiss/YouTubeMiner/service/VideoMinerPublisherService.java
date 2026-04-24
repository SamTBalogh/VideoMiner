package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.ForbiddenException;
import aiss.YouTubeMiner.model.VideoMinerModel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class VideoMinerPublisherService {

    @Autowired
    RestTemplate restTemplate;

    @Value("${videoMiner.url}")
    private String videoMinerUrl;

    public void publish(Channel channel, String token) throws ForbiddenException {
        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isBlank()) {
            headers.add("Authorization", normalizeBearerToken(token));
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Channel> requestEntity = new HttpEntity<>(channel, headers);
        try {
            restTemplate.exchange(videoMinerUrl + "/channels", HttpMethod.POST, requestEntity, Void.class);
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseVideo(e.getMessage()));
        }
    }

    public void publishAll(List<Channel> channels, String token) throws ForbiddenException {
        for (Channel channel : channels) {
            publish(channel, token);
        }
    }

    private String normalizeBearerToken(String token) {
        String trimmed = token.trim();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) {
            String value = trimmed.substring("Bearer ".length()).trim();
            return "Bearer " + value;
        }
        return "Bearer " + trimmed;
    }
}
