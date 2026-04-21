package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.ChannelNotFoundException;
import aiss.YouTubeMiner.exception.ListChannelsNotFoundException;
import aiss.YouTubeMiner.model.VideoMinerModel.Channel;
import aiss.YouTubeMiner.model.YouTubeModel.extended.channel.ChannelUploads;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ChannelServiceTest {

    @Autowired
    ChannelService service;

    @Test
    @DisplayName("Get a Channel")
    void findChannel() throws ChannelNotFoundException {
        Channel channelYouTube = service.findChannelById("UCX6OQ3DkcsbYNE6H8uQQuVA");
        assertNotNull(channelYouTube);
        System.out.println(channelYouTube);
    }
    @Test
    @DisplayName("Get a Channel 404 Not Found")
    void findChannelNotFound(){
        assertThrows(ChannelNotFoundException.class, () -> {service.findChannelById("Wololo");});
    }

    @Test
    @DisplayName("Get a Channel with its content details")
    void findChannelContentDetails() throws ChannelNotFoundException {
        ChannelUploads channelUploads = service.findChannelByIdContentDetails("UCX6OQ3DkcsbYNE6H8uQQuVA");
        Channel channel = new Channel(channelUploads);
        assertNotNull(channel);
        assertInstanceOf(Channel.class, channel);
        System.out.println(channel);
        System.out.println(channelUploads.getUploads());
    }
    @Test
    @DisplayName("Get a Channel 404 Not Found")
    void findChannelNotFoundContentDetails(){
        assertThrows(ChannelNotFoundException.class, () -> {service.findChannelByIdContentDetails("Wololo");});
    }


    @Test
    @DisplayName("Get a List of Channels by name")
    void findChannelsByName() throws ListChannelsNotFoundException, ChannelNotFoundException {
        List<ChannelUploads> channelUploads = service.findSearchListChannelsByName("ThePrimeTimeagen");
        List<Channel> channels = new ArrayList<>();
        for (ChannelUploads channelUpload : channelUploads) {
            Channel channel = new Channel(channelUpload);
            channels.add(channel);
            assertInstanceOf(Channel.class, channel);
        }
        assertNotNull(channels);
        System.out.println(channels);
    }

    @Test
    @DisplayName("Get a List of Channels by name 404 Not Found")
    void findChannelsByNameNotFound(){
        assertThrows(ListChannelsNotFoundException.class, () -> {service.findSearchListChannelsByName("askhdasdas");});
    }

    @Test
    @DisplayName("Get a List of Channels by name")
    void findChannelsByNameMax() throws ListChannelsNotFoundException, ChannelNotFoundException {
        List<ChannelUploads> channelUploads = service.findSearchListChannelsByNameMax("ThePrimeTimeagen", 2);
        List<Channel> channels = new ArrayList<>();
        for (ChannelUploads channelUpload : channelUploads) {
            Channel channel = new Channel(channelUpload);
            channels.add(channel);
            assertInstanceOf(Channel.class, channel);
        }
        assertNotNull(channels);
        Assertions.assertEquals(2, channels.size());
        System.out.println(channels);
    }

    @Test
    @DisplayName("Get a List of Channels by name 404 Not Found")
    void findChannelsByNameNotFoundMax(){
        assertThrows(ListChannelsNotFoundException.class, () -> {service.findSearchListChannelsByNameMax("askhdasdas",1);});
    }
}
