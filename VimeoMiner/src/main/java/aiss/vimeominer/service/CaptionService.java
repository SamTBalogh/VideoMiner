package aiss.vimeominer.service;

import aiss.vimeominer.exception.CaptionsNotFoundException;
import aiss.vimeominer.model.VideoMiner.Caption;
import aiss.vimeominer.model.VimeoMiner.caption.VimeoCaptionSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CaptionService {

    @Value("${vimeo.token}")
    private String token;

    @Value("${vimeo.uri}")
    private String uri;

    @Autowired
    RestTemplate restTemplate;

    public List<Caption> findCaptionsByVideoId(String id) throws CaptionsNotFoundException {

        try {
            String url = uri + "/videos/" + id + "/texttracks";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<VimeoCaptionSearch> request = new HttpEntity<>(null, headers);

            ResponseEntity<VimeoCaptionSearch> response = restTemplate.exchange(url, HttpMethod.GET, request, VimeoCaptionSearch.class);
            List<Caption> captions = response.getBody().getCaptions().stream().map(Caption::new).collect(Collectors.toList());
            return captions;
        }
        catch (HttpClientErrorException.NotFound e) {
            throw new CaptionsNotFoundException();
        }
    }

}
