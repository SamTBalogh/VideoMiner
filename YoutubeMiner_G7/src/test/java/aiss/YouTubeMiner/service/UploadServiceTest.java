package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.ChannelNotFoundException;
import aiss.YouTubeMiner.exception.UploadsNotFoundException;
import aiss.YouTubeMiner.model.YouTubeModel.extended.channel.ChannelUploads;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class UploadServiceTest {

    @Autowired
    ChannelService channelService;
    @Autowired
    UploadService service;

    @Test
    @DisplayName("Get a List of Uploads from a valid UploadsId")
    void findUploads() throws UploadsNotFoundException, ChannelNotFoundException {
        ChannelUploads channel = channelService.findChannelByIdContentDetails("UCX6OQ3DkcsbYNE6H8uQQuVA");
        List<String> videoIds = service.findUploadsIds(channel.getUploads());
        System.out.println(channel.getUploads());
        assertNotNull(videoIds);
        System.out.println(videoIds);
    }

    @Test
    @DisplayName("Get a List of Uploads of a certain size from a valid UploadsId")
    void findUploadsIdsMax() throws UploadsNotFoundException, ChannelNotFoundException {
        ChannelUploads channel = channelService.findChannelByIdContentDetails("UCX6OQ3DkcsbYNE6H8uQQuVA");
        List<String> videoIds = service.findUploadsIdsMax(channel.getUploads(), 10);
        assertNotNull(videoIds);
        assertEquals(10, videoIds.size());
        System.out.println(videoIds);
    }

    @Test
    @DisplayName("Get a List of Uploads from an invalid UploadsId")
    void findUploadsError(){
        assertThrows(HttpClientErrorException.class, () -> {service.findUploadsIds("Wololo");});
    }

    @Test
    @DisplayName("Get a List of Uploads of a certain size from an invalid UploadsId")
    void findUploadsIdsMaxError(){
        assertThrows(HttpClientErrorException.class, () -> {service.findUploadsIdsMax("Wololo", 10);});
    }

}