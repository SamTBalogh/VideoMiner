package aiss.vimeominer.controller;

import aiss.vimeominer.exception.*;
import aiss.vimeominer.model.VideoMiner.Channel;
import aiss.vimeominer.service.ChannelAssemblerService;
import aiss.vimeominer.service.VideoMinerPublisherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChannelController.class)
class ChannelControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ChannelAssemblerService channelAssemblerService;

    @MockBean
    VideoMinerPublisherService videoMinerPublisherService;

    // Required because VimeoMinerApplication defines a RestTemplate bean using RestTemplateBuilder,
    // which is not auto-configured in @WebMvcTest slice.
    @MockBean
    RestTemplateBuilder restTemplateBuilder;

    private Channel buildChannel() {
        Channel channel = new Channel("28359", "Tech Channel", "Tech content", "2024-01-01");
        channel.setVideos(Collections.emptyList());
        return channel;
    }

    @Test
    @DisplayName("GET /{id} returns 200 with channel JSON")
    void getChannel_success() throws Exception {
        when(channelAssemblerService.buildFullChannel("28359", 10, 10))
                .thenReturn(buildChannel());

        mockMvc.perform(get("/vimeoMiner/v1/28359"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("28359"))
                .andExpect(jsonPath("$.name").value("Tech Channel"));
    }

    @Test
    @DisplayName("GET /{id} with custom maxVideos and maxComments passes params to service")
    void getChannel_withParams() throws Exception {
        when(channelAssemblerService.buildFullChannel("28359", 5, 3))
                .thenReturn(buildChannel());

        mockMvc.perform(get("/vimeoMiner/v1/28359")
                        .param("maxVideos", "5")
                        .param("maxComments", "3"))
                .andExpect(status().isOk());

        verify(channelAssemblerService).buildFullChannel("28359", 5, 3);
    }

    @Test
    @DisplayName("GET /{id} returns 404 when channel not found")
    void getChannel_notFound() throws Exception {
        when(channelAssemblerService.buildFullChannel(eq("bad-id"), anyInt(), anyInt()))
                .thenThrow(new ChannelNotFoundException());

        mockMvc.perform(get("/vimeoMiner/v1/bad-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /{id} returns 404 when videos not found")
    void getChannel_videosNotFound() throws Exception {
        when(channelAssemblerService.buildFullChannel(eq("28359"), anyInt(), anyInt()))
                .thenThrow(new VideosNotFoundException());

        mockMvc.perform(get("/vimeoMiner/v1/28359"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /{id} returns 201 with channel JSON and calls publish")
    void postChannel_success() throws Exception {
        Channel channel = buildChannel();
        when(channelAssemblerService.buildFullChannel("28359", 10, 10)).thenReturn(channel);
        doNothing().when(videoMinerPublisherService).publish(any(), any());

        mockMvc.perform(post("/vimeoMiner/v1/28359")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("28359"))
                .andExpect(jsonPath("$.name").value("Tech Channel"));

        verify(videoMinerPublisherService).publish(any(Channel.class), eq("Bearer test-token"));
    }

    @Test
    @DisplayName("POST /{id} returns 404 when channel not found")
    void postChannel_notFound() throws Exception {
        when(channelAssemblerService.buildFullChannel(eq("bad-id"), anyInt(), anyInt()))
                .thenThrow(new ChannelNotFoundException());

        mockMvc.perform(post("/vimeoMiner/v1/bad-id")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /{id} returns 403 when VideoMiner rejects the request")
    void postChannel_forbidden() throws Exception {
        Channel channel = buildChannel();
        when(channelAssemblerService.buildFullChannel("28359", 10, 10)).thenReturn(channel);
        doThrow(new ForbiddenException("Unauthorized"))
                .when(videoMinerPublisherService).publish(any(), any());

        mockMvc.perform(post("/vimeoMiner/v1/28359")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isForbidden());
    }
}
