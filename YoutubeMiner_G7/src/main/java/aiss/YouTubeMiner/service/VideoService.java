package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.VideoNotFoundChannelIDException;
import aiss.YouTubeMiner.exception.VideoNotFoundException;
import aiss.YouTubeMiner.model.VideoMinerModel.Video;
import aiss.YouTubeMiner.model.YouTubeModel.extended.video.VideoSnippetNoId;
import aiss.YouTubeMiner.model.YouTubeModel.extended.video.VideoSnippetSearchNoId;
import aiss.YouTubeMiner.model.YouTubeModel.videoSnippet.VideoSnippet;
import aiss.YouTubeMiner.model.YouTubeModel.videoSnippet.VideoSnippetDetails;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoService {
    @Autowired
    RestTemplate restTemplate;

    @Value("${youtube.api.token}")
    private String token;

    @Value("${youtube.api.uri}")
    private String uri;

    public List<Video> findSearchVideosMaxChannelId(String channelId, Integer numVideos) throws VideoNotFoundChannelIDException {
        String url = uri + "/search?channelId="+channelId+"&part=snippet&type=video&maxResults="+numVideos;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-goog-api-key", token);
        HttpEntity<VideoSnippetSearch> request = new HttpEntity<>(null, headers);

        try {
            ResponseEntity<VideoSnippetSearch> response = restTemplate.exchange(url, HttpMethod.GET, request, VideoSnippetSearch.class);
            List<VideoSnippet> videoRequest = response.getBody().getItems();
            return videoRequest.stream().map(Video::new).collect(Collectors.toList());

        } catch (HttpClientErrorException.BadRequest e) {
            throw new VideoNotFoundChannelIDException();
        }

    }

    public List<Video> findVideosChannelId(String channelId) throws VideoNotFoundChannelIDException {
        String url = uri + "/search?channelId="+channelId+"&part=snippet&type=video";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-goog-api-key", token);
        HttpEntity<VideoSnippetSearch> request = new HttpEntity<>(null, headers);

        try {
            ResponseEntity<VideoSnippetSearch> response = restTemplate.exchange(url, HttpMethod.GET, request, VideoSnippetSearch.class);
            List<VideoSnippet> videoRequest = response.getBody().getItems();
            return videoRequest.stream().map(Video::new).collect(Collectors.toList());

        } catch (HttpClientErrorException.BadRequest e) {
            throw new VideoNotFoundChannelIDException();
        }

    }

    public Video findVideoById(String videoId) throws VideoNotFoundException {
        String url = uri + "/videos?part=snippet&id="+videoId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-goog-api-key", token);
        HttpEntity<VideoSnippetSearch> request = new HttpEntity<>(null, headers);

        ResponseEntity<VideoSnippetSearchNoId> response = restTemplate.exchange(url, HttpMethod.GET, request, VideoSnippetSearchNoId.class);
        List<VideoSnippetNoId> videoSnippetList = response.getBody().getItems();
        if(videoSnippetList.isEmpty()){
            throw new VideoNotFoundException();
        }
        VideoSnippetDetails details = videoSnippetList.get(0).getSnippet();
        return new Video(videoId, details.getTitle(), details.getDescription(), details.getPublishedAt());

    }

}
