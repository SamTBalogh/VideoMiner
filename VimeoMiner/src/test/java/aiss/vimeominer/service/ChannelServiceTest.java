package aiss.vimeominer.service;

import aiss.vimeominer.exception.ChannelNotFoundException;
import aiss.vimeominer.model.VideoMiner.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChannelServiceTest {

    @Autowired
    ChannelService service;

    @Test
    @DisplayName("Get channel")
    public void findChannelById() throws ChannelNotFoundException {
        Channel channel = service.findChannelById("28359");
        assertNotNull(channel);
        System.out.println(channel);
    }

    @Test
    @DisplayName("Get a Channel 404 Not Found")
    void findChannelNotFound(){
        assertThrows(ChannelNotFoundException.class, () -> {service.findChannelById("Wololo");});
    }
}