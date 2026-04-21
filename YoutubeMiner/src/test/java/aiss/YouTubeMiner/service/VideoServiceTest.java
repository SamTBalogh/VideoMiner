package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.VideoNotFoundChannelIDException;
import aiss.YouTubeMiner.exception.VideoNotFoundException;
import aiss.YouTubeMiner.model.VideoMinerModel.Video;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VideoServiceTest {
    @Autowired
    VideoService service;

    @Test
    @DisplayName("Get a certain number of videos from a valid channel Id")
    void findMaxVideosChannelId() throws VideoNotFoundChannelIDException {
        List<Video> video= service.findSearchVideosMaxChannelId("UCX6OQ3DkcsbYNE6H8uQQuVA", 3);
        assertNotNull(video);
        assertEquals(3, video.size());
        System.out.println(video);
    }

    @Test
    @DisplayName("Get a certain number videos from an invalid channel Id")
    void findMaxVideosFromInvalidChannelId()  {
        assertThrows(VideoNotFoundChannelIDException.class, () -> {service.findSearchVideosMaxChannelId("Wololo", 10);});
    }

    @Test
    @DisplayName("Get videos from a valid channel Id")
    void findVideosChannelId() throws VideoNotFoundChannelIDException {
        List<Video> video= service.findVideosChannelId("UCX6OQ3DkcsbYNE6H8uQQuVA");
        assertNotNull(video);
        System.out.println(video);
    }

    @Test
    @DisplayName("Get videos from an invalid channel Id")
    void findVideosFromInvalidChannelId()  {
        assertThrows(VideoNotFoundChannelIDException.class, () -> {service.findVideosChannelId("Wololo");});
    }

    @Test
    @DisplayName("Get Video from a valid video Id")
    void findVideosVideoId() throws VideoNotFoundException {
        Video video= service.findVideoById("T9VJKIlf5ME");
        assertNotNull(video);
        System.out.println(video);
    }

    @Test
    @DisplayName("Get Video from an invalid video Id")
    void findVideosInvalidVideoId() {
        assertThrows(VideoNotFoundException.class, () -> {service.findVideoById("Wololo");});
    }

}
