package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.CaptionNotFoundException;
import aiss.YouTubeMiner.model.VideoMinerModel.Caption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CaptionServiceTest {
    @Autowired
    CaptionService service;
    @Test
    @DisplayName("Get captions from a video id")
    void findCaptionsByVideoId() throws CaptionNotFoundException {
        List<Caption> captions = service.findCaptionsByVideoId("_VB39Jo8mAQ");
        assertNotNull(captions);
        System.out.println(captions);
    }

    @Test
    @DisplayName("Get captions from an invalid video Id")
    void findCaptionsByVideoIdNotFound() {
        assertThrows(CaptionNotFoundException.class, () -> {service.findCaptionsByVideoId("Wololo");});
    }

}
