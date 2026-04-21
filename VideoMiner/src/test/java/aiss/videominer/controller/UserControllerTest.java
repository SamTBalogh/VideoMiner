package aiss.videominer.controller;

import aiss.videominer.exception.UserNotFoundException;
import aiss.videominer.exception.VideoNotFoundException;
import aiss.videominer.model.User;
import aiss.videominer.repository.TokenRepository;
import aiss.videominer.service.TokenService;
import aiss.videominer.service.UserService;
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

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @MockBean
    TokenService tokenService;

    @MockBean
    TokenRepository tokenRepository;

    private User user1;
    private User user2;
    private static final String TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        user1 = new User("Alice", "http://alice.com", "http://alice.com/pic.jpg");
        user2 = new User("Bob", "http://bob.com", "http://bob.com/pic.jpg");
    }

    // ---- GET /users ----

    @Test
    void getAll_validToken_returns200() throws Exception {
        when(userService.findAll(anyInt(), anyInt(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/videoMiner/v1/users")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAll_noToken_returns403() throws Exception {
        doThrow(aiss.videominer.exception.TokenRequiredException.class)
                .when(tokenService).validate(any());

        mockMvc.perform(get("/videoMiner/v1/users"))
                .andExpect(status().isForbidden());
    }

    // ---- GET /users/{id} ----

    @Test
    void getById_existingId_returns200() throws Exception {
        when(userService.findById("1")).thenReturn(user1);

        mockMvc.perform(get("/videoMiner/v1/users/1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(userService.findById("bad-id")).thenThrow(UserNotFoundException.class);

        mockMvc.perform(get("/videoMiner/v1/users/bad-id")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }

    // ---- GET /videos/{videoId}/users ----

    @Test
    void getUsersByVideo_existing_returns200() throws Exception {
        when(userService.findByVideo("vid-1")).thenReturn(List.of(user1));

        mockMvc.perform(get("/videoMiner/v1/videos/vid-1/users")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getUsersByVideo_notFound_returns404() throws Exception {
        when(userService.findByVideo("bad-vid")).thenThrow(VideoNotFoundException.class);

        mockMvc.perform(get("/videoMiner/v1/videos/bad-vid/users")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }

    // ---- PUT /users/{id} ----

    @Test
    void update_existingUser_returns204() throws Exception {
        doNothing().when(userService).update(eq("1"), any(User.class));

        mockMvc.perform(put("/videoMiner/v1/users/1")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isNoContent());
    }

    @Test
    void update_notFound_returns404() throws Exception {
        doThrow(UserNotFoundException.class).when(userService).update(eq("bad-id"), any(User.class));

        mockMvc.perform(put("/videoMiner/v1/users/bad-id")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isNotFound());
    }

    // ---- DELETE /users/{id} ----

    @Test
    void delete_existingUser_returns204() throws Exception {
        doNothing().when(userService).delete("1");

        mockMvc.perform(delete("/videoMiner/v1/users/1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(UserNotFoundException.class).when(userService).delete("bad-id");

        mockMvc.perform(delete("/videoMiner/v1/users/bad-id")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }
}
