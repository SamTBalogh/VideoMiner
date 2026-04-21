package aiss.vimeominer.service;

import aiss.vimeominer.exception.VideosNotFoundException;
import aiss.vimeominer.model.VideoMiner.Video;
import aiss.vimeominer.model.VimeoMiner.video.VimeoVideoSearch;
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

    @Value("${vimeo.token}")
    private String token;

    @Value("${vimeo.uri}")
    private String uri;

    @Autowired
    RestTemplate restTemplate;

    public List<Video> findVideosByChannelId(String id) throws VideosNotFoundException {
        try {
            String url = uri + "/channels/" + id + "/videos";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<VimeoVideoSearch> request = new HttpEntity<>(null, headers);

            ResponseEntity<VimeoVideoSearch> response = restTemplate.exchange(url, HttpMethod.GET, request, VimeoVideoSearch.class);
            List<Video> videos = response.getBody().getVideos().stream().map(Video::new).collect(Collectors.toList());
            return videos;
        }
        catch (HttpClientErrorException.NotFound e) {
            throw new VideosNotFoundException();
        }
    }

    public List<Video> findVideosByChannelIdMaxVideos(String id, Integer numVideos) throws VideosNotFoundException {

        try {
            String url = uri + "/channels/" + id + "/videos?per_page=" + numVideos;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<VimeoVideoSearch> request = new HttpEntity<>(null, headers);

            ResponseEntity<VimeoVideoSearch> response = restTemplate.exchange(url, HttpMethod.GET, request, VimeoVideoSearch.class);
            List<Video> videos = response.getBody().getVideos().stream().map(Video::new).collect(Collectors.toList());
            return videos;
        }
        catch (HttpClientErrorException.NotFound e) {
            throw new VideosNotFoundException();
        }
    }
}
