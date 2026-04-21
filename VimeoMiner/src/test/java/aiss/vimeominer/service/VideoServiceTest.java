package aiss.vimeominer.service;

import aiss.vimeominer.exception.VideosNotFoundException;
import aiss.vimeominer.model.VideoMiner.Video;
import aiss.vimeominer.model.VimeoMiner.video.VimeoVideo;
import aiss.vimeominer.model.VimeoMiner.video.VimeoVideoSearch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    VideoService videoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(videoService, "token", "test-token");
        ReflectionTestUtils.setField(videoService, "uri", "https://api.vimeo.com");
    }

    private VimeoVideoSearch buildVimeoVideoSearch(String videoId, String name) {
        VimeoVideo v = new VimeoVideo();
        v.setId("/videos/" + videoId);
        v.setName(name);
        v.setDescription("desc");
        VimeoVideoSearch search = new VimeoVideoSearch();
        search.setVideos(List.of(v));
        return search;
    }

    @Test
    @DisplayName("findVideosByChannelId returns mapped videos")
    void findVideosByChannelId_success() throws VideosNotFoundException {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VimeoVideoSearch.class)))
                .thenReturn(ResponseEntity.ok(buildVimeoVideoSearch("919411397", "Sample Video")));

        List<Video> videos = videoService.findVideosByChannelId("28359");

        assertNotNull(videos);
        assertEquals(1, videos.size());
        assertEquals("919411397", videos.get(0).getId());
        assertEquals("Sample Video", videos.get(0).getName());
    }

    @Test
    @DisplayName("findVideosByChannelIdMaxVideos returns mapped videos")
    void findVideosByChannelIdMaxVideos_success() throws VideosNotFoundException {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VimeoVideoSearch.class)))
                .thenReturn(ResponseEntity.ok(buildVimeoVideoSearch("919411397", "Sample Video")));

        List<Video> videos = videoService.findVideosByChannelIdMaxVideos("28359", 5);

        assertNotNull(videos);
        assertEquals(1, videos.size());
    }

    @Test
    @DisplayName("findVideosByChannelId with invalid id throws VideosNotFoundException")
    void findVideosByChannelId_notFound() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VimeoVideoSearch.class)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThrows(VideosNotFoundException.class, () -> videoService.findVideosByChannelId("invalid"));
    }

    @Test
    @DisplayName("findVideosByChannelIdMaxVideos with invalid id throws VideosNotFoundException")
    void findVideosByChannelIdMaxVideos_notFound() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VimeoVideoSearch.class)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThrows(VideosNotFoundException.class, () -> videoService.findVideosByChannelIdMaxVideos("invalid", 10));
    }
}