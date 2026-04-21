package aiss.vimeominer.service;

import aiss.vimeominer.exception.CaptionsNotFoundException;
import aiss.vimeominer.model.VideoMiner.Caption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CaptionServiceTest {

    @Autowired
    CaptionService service;

    @Test
    @DisplayName("Get Captions")
    void findCaptionsByVideoId() throws CaptionsNotFoundException {
        List<Caption> captions = service.findCaptionsByVideoId("919411397");
        assertNotNull(captions);
        System.out.println(captions);
    }

    @Test
    @DisplayName("Get captions from an invalid video Id")
    void findCaptionsByVideoIdNotFound() {
        assertThrows(CaptionsNotFoundException.class, () -> {service.findCaptionsByVideoId("Wololo");});
    }
}