package aiss.videominer.controller;

import aiss.videominer.exception.CommentNotFoundException;
import aiss.videominer.exception.VideoNotFoundException;
import aiss.videominer.model.Comment;
import aiss.videominer.model.User;
import aiss.videominer.repository.TokenRepository;
import aiss.videominer.service.CommentService;
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

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CommentService commentService;

    @MockBean
    TokenService tokenService;

    @MockBean
    TokenRepository tokenRepository;

    private Comment comment1;
    private Comment comment2;
    private static final String TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        User author = new User("Alice", "http://alice.com", "http://alice.com/pic.jpg");
        comment1 = new Comment("com-1", "Hello world", "2024-01-01", author);
        comment2 = new Comment("com-2", "Second comment", "2024-01-02", author);
    }

    // ---- GET /comments ----

    @Test
    void getAll_validToken_returns200() throws Exception {
        when(commentService.findAll(anyInt(), anyInt(), any(), any(), any(), any()))
                .thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/videoMiner/v1/comments")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAll_noToken_returns403() throws Exception {
        doThrow(aiss.videominer.exception.TokenRequiredException.class)
                .when(tokenService).validate(any());

        mockMvc.perform(get("/videoMiner/v1/comments"))
                .andExpect(status().isForbidden());
    }

    // ---- GET /comments/{id} ----

    @Test
    void getById_existingId_returns200() throws Exception {
        when(commentService.findById("com-1")).thenReturn(comment1);

        mockMvc.perform(get("/videoMiner/v1/comments/com-1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("com-1"))
                .andExpect(jsonPath("$.text").value("Hello world"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(commentService.findById("bad-id")).thenThrow(CommentNotFoundException.class);

        mockMvc.perform(get("/videoMiner/v1/comments/bad-id")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }

    // ---- GET /videos/{videoId}/comments ----

    @Test
    void getCommentsByVideo_existing_returns200() throws Exception {
        when(commentService.findByVideo("vid-1")).thenReturn(List.of(comment1));

        mockMvc.perform(get("/videoMiner/v1/videos/vid-1/comments")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getCommentsByVideo_notFound_returns404() throws Exception {
        when(commentService.findByVideo("bad-vid")).thenThrow(VideoNotFoundException.class);

        mockMvc.perform(get("/videoMiner/v1/videos/bad-vid/comments")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }

    // ---- POST /videos/{videoId}/comments ----

    @Test
    void create_validComment_returns201() throws Exception {
        when(commentService.create(eq("vid-1"), any(Comment.class))).thenReturn(List.of(comment1));

        mockMvc.perform(post("/videoMiner/v1/videos/vid-1/comments")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment1)))
                .andExpect(status().isCreated());
    }

    // ---- PUT /comments/{id} ----

    @Test
    void update_existingComment_returns204() throws Exception {
        doNothing().when(commentService).update(eq("com-1"), any(Comment.class));

        mockMvc.perform(put("/videoMiner/v1/comments/com-1")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment1)))
                .andExpect(status().isNoContent());
    }

    @Test
    void update_notFound_returns404() throws Exception {
        doThrow(CommentNotFoundException.class).when(commentService).update(eq("bad-id"), any(Comment.class));

        mockMvc.perform(put("/videoMiner/v1/comments/bad-id")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment1)))
                .andExpect(status().isNotFound());
    }

    // ---- DELETE /comments/{id} ----

    @Test
    void delete_existingComment_returns204() throws Exception {
        doNothing().when(commentService).delete("com-1");

        mockMvc.perform(delete("/videoMiner/v1/comments/com-1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(CommentNotFoundException.class).when(commentService).delete("bad-id");

        mockMvc.perform(delete("/videoMiner/v1/comments/bad-id")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }
}
