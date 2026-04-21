package aiss.videominer.controller;

import aiss.videominer.exception.CaptionNotFoundException;
import aiss.videominer.exception.VideoNotFoundException;
import aiss.videominer.model.Caption;
import aiss.videominer.model.Token;
import aiss.videominer.repository.TokenRepository;
import aiss.videominer.service.CaptionService;
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

@WebMvcTest(CaptionController.class)
class CaptionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CaptionService captionService;

    @MockBean
    TokenService tokenService;

    // TokenRepository is needed by the Spring context even if TokenService is mocked
    @MockBean
    TokenRepository tokenRepository;

    private Caption caption1;
    private Caption caption2;
    private static final String TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        caption1 = new Caption("cap-1", "en", "English");
        caption2 = new Caption("cap-2", "es", "Spanish");
    }

    // ---- GET /captions ----

    @Test
    void getAll_validToken_returns200() throws Exception {
        when(captionService.findAll(anyInt(), anyInt(), any(), any(), any(), any()))
                .thenReturn(List.of(caption1, caption2));

        mockMvc.perform(get("/videoMiner/v1/captions")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAll_noToken_returns403() throws Exception {
        doThrow(aiss.videominer.exception.TokenRequiredException.class)
                .when(tokenService).validate(any());

        mockMvc.perform(get("/videoMiner/v1/captions"))
                .andExpect(status().isForbidden());
    }

    // ---- GET /captions/{id} ----

    @Test
    void getById_existingId_returns200() throws Exception {
        when(captionService.findById("cap-1")).thenReturn(caption1);

        mockMvc.perform(get("/videoMiner/v1/captions/cap-1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cap-1"))
                .andExpect(jsonPath("$.language").value("en"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(captionService.findById("bad-id")).thenThrow(CaptionNotFoundException.class);

        mockMvc.perform(get("/videoMiner/v1/captions/bad-id")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }

    // ---- GET /videos/{videoId}/captions ----

    @Test
    void getCaptionsByVideo_existing_returns200() throws Exception {
        when(captionService.findByVideo("vid-1")).thenReturn(List.of(caption1));

        mockMvc.perform(get("/videoMiner/v1/videos/vid-1/captions")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getCaptionsByVideo_videoNotFound_returns404() throws Exception {
        when(captionService.findByVideo("bad-vid")).thenThrow(VideoNotFoundException.class);

        mockMvc.perform(get("/videoMiner/v1/videos/bad-vid/captions")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }

    // ---- POST /videos/{videoId}/captions ----

    @Test
    void create_validCaption_returns201() throws Exception {
        when(captionService.create(eq("vid-1"), any(Caption.class))).thenReturn(List.of(caption1));

        mockMvc.perform(post("/videoMiner/v1/videos/vid-1/captions")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(caption1)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_videoNotFound_returns404() throws Exception {
        when(captionService.create(eq("bad-vid"), any(Caption.class)))
                .thenThrow(VideoNotFoundException.class);

        mockMvc.perform(post("/videoMiner/v1/videos/bad-vid/captions")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(caption1)))
                .andExpect(status().isNotFound());
    }

    // ---- PUT /captions/{id} ----

    @Test
    void update_existingCaption_returns204() throws Exception {
        doNothing().when(captionService).update(eq("cap-1"), any(Caption.class));

        mockMvc.perform(put("/videoMiner/v1/captions/cap-1")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(caption1)))
                .andExpect(status().isNoContent());
    }

    @Test
    void update_notFound_returns404() throws Exception {
        doThrow(CaptionNotFoundException.class).when(captionService).update(eq("bad-id"), any(Caption.class));

        mockMvc.perform(put("/videoMiner/v1/captions/bad-id")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(caption1)))
                .andExpect(status().isNotFound());
    }

    // ---- DELETE /captions/{id} ----

    @Test
    void delete_existingCaption_returns204() throws Exception {
        doNothing().when(captionService).delete("cap-1");

        mockMvc.perform(delete("/videoMiner/v1/captions/cap-1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(CaptionNotFoundException.class).when(captionService).delete("bad-id");

        mockMvc.perform(delete("/videoMiner/v1/captions/bad-id")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }
}
