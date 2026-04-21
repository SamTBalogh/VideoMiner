package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.UploadsNotFoundException;
import aiss.YouTubeMiner.model.YouTubeModel.extended.uploads.ResourceId;
import aiss.YouTubeMiner.model.YouTubeModel.extended.uploads.UploadItems;
import aiss.YouTubeMiner.model.YouTubeModel.extended.uploads.UploadSnippet;
import aiss.YouTubeMiner.model.YouTubeModel.extended.uploads.Uploads;
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
class UploadServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    UploadService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "token", "test-token");
        ReflectionTestUtils.setField(service, "uri", "https://youtube.googleapis.com/youtube/v3");
    }

    private Uploads buildUploads(String... videoIds) {
        Uploads uploads = new Uploads();
        List<UploadItems> items = java.util.Arrays.stream(videoIds).map(id -> {
            ResourceId resourceId = new ResourceId();
            resourceId.setVideoId(id);
            UploadSnippet snippet = new UploadSnippet();
            snippet.setResourceId(resourceId);
            UploadItems item = new UploadItems();
            item.setSnippet(snippet);
            return item;
        }).toList();
        uploads.setItems(items);
        return uploads;
    }

    @Test
    @DisplayName("findUploadsIdsMax returns list of video IDs")
    void findUploadsIdsMax_success() throws UploadsNotFoundException {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Uploads.class)))
                .thenReturn(ResponseEntity.ok(buildUploads("vid1", "vid2", "vid3")));

        List<String> videoIds = service.findUploadsIdsMax("UUabc123", 3);
        assertNotNull(videoIds);
        assertEquals(3, videoIds.size());
        assertTrue(videoIds.contains("vid1"));
    }

    @Test
    @DisplayName("findUploadsIdsMax throws UploadsNotFoundException on 400")
    void findUploadsIdsMax_badRequest() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Uploads.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null));

        assertThrows(UploadsNotFoundException.class, () -> service.findUploadsIdsMax("invalid", 5));
    }
}
