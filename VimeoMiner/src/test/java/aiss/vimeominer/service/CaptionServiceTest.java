package aiss.vimeominer.service;

import aiss.vimeominer.exception.CaptionsNotFoundException;
import aiss.vimeominer.model.VideoMiner.Caption;
import aiss.vimeominer.model.VimeoMiner.caption.VimeoCaption;
import aiss.vimeominer.model.VimeoMiner.caption.VimeoCaptionSearch;
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
class CaptionServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    CaptionService captionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(captionService, "token", "test-token");
        ReflectionTestUtils.setField(captionService, "uri", "https://api.vimeo.com");
    }

    private VimeoCaptionSearch buildVimeoCaptionSearch() {
        VimeoCaption c = new VimeoCaption();
        c.setId("cap-1");
        c.setName("English");
        c.setLanguage("en");
        VimeoCaptionSearch search = new VimeoCaptionSearch();
        search.setCaptions(List.of(c));
        return search;
    }

    @Test
    @DisplayName("findCaptionsByVideoId returns mapped captions")
    void findCaptionsByVideoId_success() throws CaptionsNotFoundException {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VimeoCaptionSearch.class)))
                .thenReturn(ResponseEntity.ok(buildVimeoCaptionSearch()));

        List<Caption> captions = captionService.findCaptionsByVideoId("919411397");

        assertNotNull(captions);
        assertEquals(1, captions.size());
        assertEquals("cap-1", captions.get(0).getId());
        assertEquals("en", captions.get(0).getLanguage());
    }

    @Test
    @DisplayName("findCaptionsByVideoId with invalid id throws CaptionsNotFoundException")
    void findCaptionsByVideoId_notFound() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VimeoCaptionSearch.class)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThrows(CaptionsNotFoundException.class, () -> captionService.findCaptionsByVideoId("invalid"));
    }
}
