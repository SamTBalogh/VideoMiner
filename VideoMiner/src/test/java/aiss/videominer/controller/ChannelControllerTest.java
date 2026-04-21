package aiss.videominer.controller;

import aiss.videominer.exception.ChannelNotFoundException;
import aiss.videominer.model.Channel;
import aiss.videominer.repository.TokenRepository;
import aiss.videominer.service.ChannelService;
import aiss.videominer.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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
    ChannelService channelService;

    @MockBean
    TokenService tokenService;

    @MockBean
    TokenRepository tokenRepository;

    private Channel channel1;
    private Channel channel2;
    private static final String TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        channel1 = new Channel("ch-1", "Channel One", "Description 1", "2024-01-01");
        channel2 = new Channel("ch-2", "Channel Two", "Description 2", "2024-02-01");
    }

    // ---- GET /channels ----

    @Test
    void getAll_validToken_returns200() throws Exception {
        when(channelService.findAll(anyInt(), anyInt(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(channel1, channel2));

        mockMvc.perform(get("/videoMiner/v1/channels")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAll_noToken_returns403() throws Exception {
        doThrow(aiss.videominer.exception.TokenRequiredException.class)
                .when(tokenService).validate(any());

        mockMvc.perform(get("/videoMiner/v1/channels"))
                .andExpect(status().isForbidden());
    }

    // ---- GET /channels/{id} ----

    @Test
    void getById_existingId_returns200() throws Exception {
        when(channelService.findById("ch-1")).thenReturn(channel1);

        mockMvc.perform(get("/videoMiner/v1/channels/ch-1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ch-1"))
                .andExpect(jsonPath("$.name").value("Channel One"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(channelService.findById("bad-id")).thenThrow(ChannelNotFoundException.class);

        mockMvc.perform(get("/videoMiner/v1/channels/bad-id")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }

    // ---- POST /channels ----

    @Test
    void create_validChannel_returns201() throws Exception {
        when(channelService.create(any(Channel.class))).thenReturn(channel1);

        mockMvc.perform(post("/videoMiner/v1/channels")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(channel1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("ch-1"));
    }

    // ---- PUT /channels/{id} ----

    @Test
    void update_existingChannel_returns204() throws Exception {
        doNothing().when(channelService).update(eq("ch-1"), any(Channel.class));

        mockMvc.perform(put("/videoMiner/v1/channels/ch-1")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(channel1)))
                .andExpect(status().isNoContent());
    }

    @Test
    void update_notFound_returns404() throws Exception {
        doThrow(ChannelNotFoundException.class).when(channelService).update(eq("bad-id"), any(Channel.class));

        mockMvc.perform(put("/videoMiner/v1/channels/bad-id")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(channel1)))
                .andExpect(status().isNotFound());
    }

    // ---- DELETE /channels/{id} ----

    @Test
    void delete_existingChannel_returns204() throws Exception {
        doNothing().when(channelService).delete("ch-1");

        mockMvc.perform(delete("/videoMiner/v1/channels/ch-1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(ChannelNotFoundException.class).when(channelService).delete("bad-id");

        mockMvc.perform(delete("/videoMiner/v1/channels/bad-id")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }
}
