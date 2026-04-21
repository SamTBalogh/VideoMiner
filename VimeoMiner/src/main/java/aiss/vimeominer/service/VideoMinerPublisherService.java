package aiss.vimeominer.service;

import aiss.vimeominer.exception.ForbiddenException;
import aiss.vimeominer.model.VideoMiner.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class VideoMinerPublisherService {

    @Value("${videoMiner.url}")
    private String videoMinerUrl;

    @Autowired
    private RestTemplate restTemplate;

    public void publish(Channel channel, String token) throws ForbiddenException {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (token != null) {
                headers.add("Authorization", token);
            }
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Channel> requestEntity = new HttpEntity<>(channel, headers);
            restTemplate.exchange(videoMinerUrl + "/channels", HttpMethod.POST, requestEntity, Void.class);
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseVideo(e.getMessage()));
        }
    }
}
