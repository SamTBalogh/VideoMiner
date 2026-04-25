package aiss.YouTubeMiner.Controller;

import aiss.YouTubeMiner.exception.ChannelNotFoundException;
import aiss.YouTubeMiner.exception.ForbiddenException;
import aiss.YouTubeMiner.model.VideoMinerModel.Channel;
import aiss.YouTubeMiner.service.ChannelAssemblerService;
import aiss.YouTubeMiner.service.VideoMinerPublisherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChannelController.class)
class ChannelControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ChannelAssemblerService channelAssemblerService;

    @MockBean
    VideoMinerPublisherService videoMinerPublisherService;

    @MockBean
    RestTemplateBuilder restTemplateBuilder;

    private Channel sampleChannel() {
        return new Channel("ch1", "Test Channel", "Description", "2024-01-01");
    }

    // ---- v1 single channel ----

    @Test
    @DisplayName("GET /youTubeMiner/v1/{id} returns 200 with channel JSON")
    void getChannelV1_success() throws Exception {
        when(channelAssemblerService.buildFullChannelV1(eq("ch1"), anyInt(), anyInt()))
                .thenReturn(sampleChannel());

        mockMvc.perform(get("/youTubeMiner/v1/ch1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("ch1"));
    }

    @Test
    @DisplayName("GET /youTubeMiner/v1/{id} returns 404 when channel not found")
    void getChannelV1_notFound() throws Exception {
        when(channelAssemblerService.buildFullChannelV1(eq("unknown"), anyInt(), anyInt()))
                .thenThrow(new ChannelNotFoundException());

        mockMvc.perform(get("/youTubeMiner/v1/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /youTubeMiner/v1/{id} returns 201 with channel JSON")
    void postChannelV1_success() throws Exception {
        when(channelAssemblerService.buildFullChannelV1(eq("ch1"), anyInt(), anyInt()))
                .thenReturn(sampleChannel());

        mockMvc.perform(post("/youTubeMiner/v1/ch1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("ch1"));
    }

    @Test
    @DisplayName("POST /youTubeMiner/v1/{id} returns 403 when publisher throws ForbiddenException")
    void postChannelV1_forbidden() throws Exception {
        when(channelAssemblerService.buildFullChannelV1(eq("ch1"), anyInt(), anyInt()))
                .thenReturn(sampleChannel());
        org.mockito.Mockito.doThrow(new ForbiddenException("Unauthorized"))
                .when(videoMinerPublisherService).publish(any(), any());

        mockMvc.perform(post("/youTubeMiner/v1/ch1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isForbidden());
    }

    // ---- v1 channel list ----

    @Test
    @DisplayName("GET /youTubeMiner/v1/channels returns 200 with channel list")
    void getChannelListV1_success() throws Exception {
        when(channelAssemblerService.buildFullChannelListV1(eq("test"), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(sampleChannel()));

        mockMvc.perform(get("/youTubeMiner/v1/channels").param("name", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("ch1"));
    }

    @Test
    @DisplayName("POST /youTubeMiner/v1/channels returns 201 with channel list")
    void postChannelListV1_success() throws Exception {
        when(channelAssemblerService.buildFullChannelListV1(eq("test"), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(sampleChannel()));

        mockMvc.perform(post("/youTubeMiner/v1/channels")
                        .param("name", "test")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value("ch1"));
    }

    // ---- v2 single channel ----

    @Test
    @DisplayName("GET /youTubeMiner/v2/{id} returns 200 with channel JSON")
    void getChannelV2_success() throws Exception {
        when(channelAssemblerService.buildFullChannelV2(eq("ch1"), anyInt(), anyInt()))
                .thenReturn(sampleChannel());

        mockMvc.perform(get("/youTubeMiner/v2/ch1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ch1"));
    }

    @Test
    @DisplayName("POST /youTubeMiner/v2/{id} returns 201 with channel JSON")
    void postChannelV2_success() throws Exception {
        when(channelAssemblerService.buildFullChannelV2(eq("ch1"), anyInt(), anyInt()))
                .thenReturn(sampleChannel());

        mockMvc.perform(post("/youTubeMiner/v2/ch1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("ch1"));
    }

    // ---- v2 channel list ----

    @Test
    @DisplayName("GET /youTubeMiner/v2/channels returns 200 with channel list")
    void getChannelListV2_success() throws Exception {
        when(channelAssemblerService.buildFullChannelListV2(eq("test"), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(sampleChannel()));

        mockMvc.perform(get("/youTubeMiner/v2/channels").param("name", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("ch1"));
    }

    @Test
    @DisplayName("POST /youTubeMiner/v2/channels returns 201 with channel list")
    void postChannelListV2_success() throws Exception {
        when(channelAssemblerService.buildFullChannelListV2(eq("test"), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(sampleChannel()));

        mockMvc.perform(post("/youTubeMiner/v2/channels")
                        .param("name", "test")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value("ch1"));
    }
}
