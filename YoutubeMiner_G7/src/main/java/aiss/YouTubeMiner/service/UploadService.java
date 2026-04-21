package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.UploadsNotFoundException;
import aiss.YouTubeMiner.model.YouTubeModel.extended.uploads.UploadItems;
import aiss.YouTubeMiner.model.YouTubeModel.extended.uploads.Uploads;
import aiss.YouTubeMiner.model.YouTubeModel.videoSnippet.VideoSnippetSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class UploadService {
    @Autowired
    RestTemplate restTemplate;

    @Value("${youtube.api.token}")
    private String token;

    @Value("${youtube.api.uri}")
    private String uri;

    public List<String> findUploadsIds(String uploadsId) throws UploadsNotFoundException {
        String url = uri + "/playlistItems?playlistId="+uploadsId+"&part=snippet";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-goog-api-key", token);
        HttpEntity<VideoSnippetSearch> request = new HttpEntity<>(null, headers);

        try {
            ResponseEntity<Uploads> response = restTemplate.exchange(url, HttpMethod.GET, request, Uploads.class);
            List<String> videosIds = new ArrayList<>();
            List<UploadItems> resourceIds = response.getBody().getItems();
            for(UploadItems uploadItems : resourceIds){
                videosIds.add(uploadItems.getSnippet().getResourceId().getVideoId());
            }
            return videosIds;

        } catch (HttpClientErrorException.BadRequest e) {
            throw new UploadsNotFoundException();
        }

    }

    public List<String> findUploadsIdsMax(String uploadsId, Integer maxResults) throws UploadsNotFoundException {
        String url = uri + "/playlistItems?playlistId="+uploadsId+"&part=snippet&maxResults="+maxResults;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-goog-api-key", token);
        HttpEntity<VideoSnippetSearch> request = new HttpEntity<>(null, headers);

        try {
            ResponseEntity<Uploads> response = restTemplate.exchange(url, HttpMethod.GET, request, Uploads.class);
            List<String> videosIds = new ArrayList<>();
            List<UploadItems> resourceIds = response.getBody().getItems();
            for(UploadItems uploadItems : resourceIds){
                videosIds.add(uploadItems.getSnippet().getResourceId().getVideoId());
            }
            return videosIds;

        } catch (HttpClientErrorException.BadRequest e) {
            throw new UploadsNotFoundException();
        }

    }
}
