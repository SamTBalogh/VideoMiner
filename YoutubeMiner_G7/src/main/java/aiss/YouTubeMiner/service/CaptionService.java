package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.CaptionNotFoundException;
import aiss.YouTubeMiner.model.YouTubeModel.caption.CaptionSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import aiss.YouTubeMiner.model.VideoMinerModel.Caption;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;
@Service
public class CaptionService {
    @Value("${youtube.api.token}")
    private String token;

    @Value("${youtube.api.uri}")
    private String uri;

    @Autowired
    RestTemplate restTemplate;

    public List<Caption> findCaptionsByVideoId(String id) throws CaptionNotFoundException {

        try {
            String url = uri+"/captions?part=snippet&videoId="+id;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-goog-api-key", token);
            HttpEntity<CaptionSearch> request = new HttpEntity<>(null, headers);

            ResponseEntity<CaptionSearch> response = restTemplate.exchange(url, HttpMethod.GET, request,CaptionSearch.class);
            return response.getBody().getItems().stream().map(Caption::new).collect(Collectors.toList());
        }
        catch (HttpClientErrorException.NotFound e){
            throw new CaptionNotFoundException();
        }
    }
}
