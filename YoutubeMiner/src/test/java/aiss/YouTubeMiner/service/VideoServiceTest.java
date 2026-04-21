package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.VideoNotFoundChannelIDException;
import aiss.YouTubeMiner.exception.VideoNotFoundException;
import aiss.YouTubeMiner.model.VideoMinerModel.Video;
import aiss.YouTubeMiner.model.YouTubeModel.extended.video.VideoSnippetNoId;
import aiss.YouTubeMiner.model.YouTubeModel.extended.video.VideoSnippetSearchNoId;
import aiss.YouTubeMiner.model.YouTubeModel.videoSnippet.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VideoServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    VideoService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "token", "test-token");
        ReflectionTestUtils.setField(service, "uri", "https://youtube.googleapis.com/youtube/v3");
    }

    private VideoSnippetSearch buildVideoSnippetSearch(String videoId, String title) {
        VideoSnippetId id = new VideoSnippetId();
        id.setVideoId(videoId);
        VideoSnippetDetails details = new VideoSnippetDetails();
        details.setTitle(title);
        details.setDescription("A description");
        details.setPublishedAt("2023-01-01T00:00:00Z");
        VideoSnippet snippet = new VideoSnippet();
        snippet.setId(id);
        snippet.setSnippet(details);
        VideoSnippetSearch search = new VideoSnippetSearch();
        search.setItems(List.of(snippet));
        return search;
    }

    private VideoSnippetSearchNoId buildVideoSnippetSearchNoId(String videoId, String title) {
        VideoSnippetDetails details = new VideoSnippetDetails();
        details.setTitle(title);
        details.setDescription("A description");
        details.setPublishedAt("2023-01-01T00:00:00Z");
        VideoSnippetNoId noId = new VideoSnippetNoId();
        noId.setSnippet(details);
        VideoSnippetSearchNoId searchNoId = new VideoSnippetSearchNoId();
        searchNoId.setItems(List.of(noId));
        return searchNoId;
    }

    @Test
    @DisplayName("findSearchVideosMaxChannelId returns list of videos")
    void findSearchVideosMaxChannelId_success() throws VideoNotFoundChannelIDException {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VideoSnippetSearch.class)))
                .thenReturn(ResponseEntity.ok(buildVideoSnippetSearch("v1", "Video One")));

        List<Video> videos = service.findSearchVideosMaxChannelId("ch1", 3);
        assertNotNull(videos);
        assertEquals(1, videos.size());
        assertEquals("v1", videos.get(0).getId());
    }

    @Test
    @DisplayName("findSearchVideosMaxChannelId throws VideoNotFoundChannelIDException on BadRequest")
    void findSearchVideosMaxChannelId_badRequest() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VideoSnippetSearch.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null));

        assertThrows(VideoNotFoundChannelIDException.class,
                () -> service.findSearchVideosMaxChannelId("invalid", 5));
    }

    @Test
    @DisplayName("findVideoById returns a Video")
    void findVideoById_success() throws VideoNotFoundException {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VideoSnippetSearchNoId.class)))
                .thenReturn(ResponseEntity.ok(buildVideoSnippetSearchNoId("v1", "Video One")));

        Video video = service.findVideoById("v1");
        assertNotNull(video);
        assertEquals("v1", video.getId());
        assertEquals("Video One", video.getName());
    }

    @Test
    @DisplayName("findVideoById throws VideoNotFoundException when list is empty")
    void findVideoById_notFound() {
        VideoSnippetSearchNoId emptyResponse = new VideoSnippetSearchNoId();
        emptyResponse.setItems(List.of());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VideoSnippetSearchNoId.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        assertThrows(VideoNotFoundException.class, () -> service.findVideoById("unknown"));
    }
}
