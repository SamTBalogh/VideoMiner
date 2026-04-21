package aiss.vimeominer.service;

import aiss.vimeominer.exception.VideosNotFoundException;
import aiss.vimeominer.model.VideoMiner.Video;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class VideoServiceTest {

    @Autowired
    VideoService service;

    @Test
    @DisplayName("Get Videos")
    void findVideosByChannelId() throws VideosNotFoundException {
        List<Video> videos = service.findVideosByChannelId("28359");
        assertNotNull(videos);
        System.out.println(videos);
    }

    @Test
    @DisplayName("Get Videos Max Number")
    void findVideosByChannelIdMaxVideos() throws VideosNotFoundException {
        List<Video> videos = service.findVideosByChannelIdMaxVideos("28359", 10);
        assertNotNull(videos);
        System.out.println("The number of videos in the list is "+videos.size());
        System.out.println(videos);
    }

    @Test
    @DisplayName("Get Videos Error 404")
    void findVideosByChannelIdNotFound() {
        assertThrows(VideosNotFoundException.class, () -> {service.findVideosByChannelId("Wololo");});
    }

    @Test
    @DisplayName("Get Videos Error 404")
    void findVideosByChannelIdMaxVideosNotFound() {
        assertThrows(VideosNotFoundException.class, () -> {service.findVideosByChannelIdMaxVideos("Wololo", 10);});
    }
}