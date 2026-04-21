package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.CaptionNotFoundException;
import aiss.YouTubeMiner.model.VideoMinerModel.Caption;
import aiss.YouTubeMiner.model.YouTubeModel.caption.CaptionSearch;
import aiss.YouTubeMiner.model.YouTubeModel.caption.CaptionSnippet;
import aiss.YouTubeMiner.model.YouTubeModel.caption.Captions;
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
class CaptionServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    CaptionService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "token", "test-token");
        ReflectionTestUtils.setField(service, "uri", "https://youtube.googleapis.com/youtube/v3");
    }

    private CaptionSearch buildCaptionSearch(String captionId, String language, String name) {
        CaptionSnippet snippet = new CaptionSnippet();
        snippet.setLanguage(language);
        snippet.setName(name);
        Captions captions = new Captions();
        captions.setId(captionId);
        captions.setSnippet(snippet);
        CaptionSearch captionSearch = new CaptionSearch();
        captionSearch.setItems(List.of(captions));
        return captionSearch;
    }

    @Test
    @DisplayName("findCaptionsByVideoId returns list of captions")
    void findCaptionsByVideoId_success() throws CaptionNotFoundException {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(CaptionSearch.class)))
                .thenReturn(ResponseEntity.ok(buildCaptionSearch("cap1", "en", "English")));

        List<Caption> captions = service.findCaptionsByVideoId("v1");
        assertNotNull(captions);
        assertEquals(1, captions.size());
        assertEquals("cap1", captions.get(0).getId());
    }

    @Test
    @DisplayName("findCaptionsByVideoId throws CaptionNotFoundException on 404")
    void findCaptionsByVideoId_notFound() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(CaptionSearch.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThrows(CaptionNotFoundException.class, () -> service.findCaptionsByVideoId("unknown"));
    }
}
