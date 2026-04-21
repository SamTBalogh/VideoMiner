package aiss.vimeominer.service;

import aiss.vimeominer.exception.ChannelNotFoundException;
import aiss.vimeominer.model.VideoMiner.Channel;
import aiss.vimeominer.model.VimeoMiner.channel.VimeoChannel;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    ChannelService channelService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(channelService, "token", "test-token");
        ReflectionTestUtils.setField(channelService, "uri", "https://api.vimeo.com");
    }

    @Test
    @DisplayName("Get channel by id returns mapped Channel")
    void findChannelById_success() throws ChannelNotFoundException {
        VimeoChannel vimeoChannel = new VimeoChannel();
        vimeoChannel.setName("Tech Channel");
        vimeoChannel.setDescription("Tech content");
        vimeoChannel.setCreatedTime("2024-01-01T00:00:00+00:00");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VimeoChannel.class)))
                .thenReturn(ResponseEntity.ok(vimeoChannel));

        Channel result = channelService.findChannelById("28359");

        assertNotNull(result);
        assertEquals("28359", result.getId());
        assertEquals("Tech Channel", result.getName());
        assertEquals("Tech content", result.getDescription());
    }

    @Test
    @DisplayName("Get channel with invalid id throws ChannelNotFoundException")
    void findChannelById_notFound() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VimeoChannel.class)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThrows(ChannelNotFoundException.class, () -> channelService.findChannelById("invalid-id"));
    }
}
