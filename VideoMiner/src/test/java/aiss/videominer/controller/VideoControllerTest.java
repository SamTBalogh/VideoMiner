package aiss.videominer.controller;

import aiss.videominer.exception.ChannelNotFoundException;
import aiss.videominer.exception.VideoNotFoundException;
import aiss.videominer.model.Video;
import aiss.videominer.repository.TokenRepository;
import aiss.videominer.service.TokenService;
import aiss.videominer.service.VideoService;
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

@WebMvcTest(VideoController.class)
class VideoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    VideoService videoService;

    @MockBean
    TokenService tokenService;

    @MockBean
    TokenRepository tokenRepository;

    private Video video1;
    private Video video2;
    private static final String TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        video1 = new Video("vid-1", "Video One", "Desc 1", "2024-01-01");
        video2 = new Video("vid-2", "Video Two", "Desc 2", "2024-02-01");
    }

    // ---- GET /videos ----

    @Test
    void getAll_validToken_returns200() throws Exception {
        when(videoService.findAll(anyInt(), anyInt(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(video1, video2));

        mockMvc.perform(get("/videoMiner/v1/videos")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAll_noToken_returns403() throws Exception {
        doThrow(aiss.videominer.exception.TokenRequiredException.class)
                .when(tokenService).validate(any());

        mockMvc.perform(get("/videoMiner/v1/videos"))
                .andExpect(status().isForbidden());
    }

    // ---- GET /videos/{id} ----

    @Test
    void getById_existingId_returns200() throws Exception {
        when(videoService.findById("vid-1")).thenReturn(video1);

        mockMvc.perform(get("/videoMiner/v1/videos/vid-1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("vid-1"))
                .andExpect(jsonPath("$.name").value("Video One"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(videoService.findById("bad-id")).thenThrow(VideoNotFoundException.class);

        mockMvc.perform(get("/videoMiner/v1/videos/bad-id")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }

    // ---- GET /channels/{channelId}/videos ----

    @Test
    void getVideosByChannel_existing_returns200() throws Exception {
        when(videoService.findByChannel("ch-1")).thenReturn(List.of(video1));

        mockMvc.perform(get("/videoMiner/v1/channels/ch-1/videos")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getVideosByChannel_notFound_returns404() throws Exception {
        when(videoService.findByChannel("bad-ch")).thenThrow(ChannelNotFoundException.class);

        mockMvc.perform(get("/videoMiner/v1/channels/bad-ch/videos")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }

    // ---- POST /channels/{channelId}/videos ----

    @Test
    void create_validVideo_returns201() throws Exception {
        when(videoService.create(eq("ch-1"), any(Video.class))).thenReturn(List.of(video1));

        mockMvc.perform(post("/videoMiner/v1/channels/ch-1/videos")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(video1)))
                .andExpect(status().isCreated());
    }

    // ---- PUT /videos/{id} ----

    @Test
    void update_existingVideo_returns204() throws Exception {
        doNothing().when(videoService).update(eq("vid-1"), any(Video.class));

        mockMvc.perform(put("/videoMiner/v1/videos/vid-1")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(video1)))
                .andExpect(status().isNoContent());
    }

    @Test
    void update_notFound_returns404() throws Exception {
        doThrow(VideoNotFoundException.class).when(videoService).update(eq("bad-id"), any(Video.class));

        mockMvc.perform(put("/videoMiner/v1/videos/bad-id")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(video1)))
                .andExpect(status().isNotFound());
    }

    // ---- DELETE /videos/{id} ----

    @Test
    void delete_existingVideo_returns204() throws Exception {
        doNothing().when(videoService).delete("vid-1");

        mockMvc.perform(delete("/videoMiner/v1/videos/vid-1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(VideoNotFoundException.class).when(videoService).delete("bad-id");

        mockMvc.perform(delete("/videoMiner/v1/videos/bad-id")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }
}
