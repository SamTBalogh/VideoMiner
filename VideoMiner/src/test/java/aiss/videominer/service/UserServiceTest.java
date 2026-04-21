package aiss.videominer.service;

import aiss.videominer.exception.*;
import aiss.videominer.model.Comment;
import aiss.videominer.model.User;
import aiss.videominer.model.Video;
import aiss.videominer.repository.CommentRepository;
import aiss.videominer.repository.UserRepository;
import aiss.videominer.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    VideoRepository videoRepository;

    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    UserService userService;

    private User user1;
    private User user2;
    private Comment comment;
    private Video video;

    @BeforeEach
    void setUp() {
        user1 = new User("Alice", "http://alice.com", "http://alice.com/pic.jpg");
        user2 = new User("Bob", "http://bob.com", "http://bob.com/pic.jpg");

        comment = new Comment("com-1", "Hello", "2024-01-01", user1);

        video = new Video("vid-1", "Test Video", "desc", "2024-01-01");
        video.getComments().add(comment);
    }

    // ---- findAll ----

    @Test
    void findAll_noFilter_returnsAll() throws BadRequestParameterField, BadRequestIdParameter {
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user1, user2)));

        List<User> result = userService.findAll(0, 10, null, null, null, null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void findAll_filterByName_returnsMatch() throws BadRequestParameterField, BadRequestIdParameter {
        when(userRepository.findByName(eq("Alice"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user1)));

        List<User> result = userService.findAll(0, 10, null, "Alice", null, null, null);

        assertThat(result).containsExactly(user1);
    }

    @Test
    void findAll_filterByUserLink_returnsMatch() throws BadRequestParameterField, BadRequestIdParameter {
        when(userRepository.findByUserLinkContaining(eq("alice"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user1)));

        List<User> result = userService.findAll(0, 10, null, null, "alice", null, null);

        assertThat(result).containsExactly(user1);
    }

    @Test
    void findAll_filterByPictureLink_returnsMatch() throws BadRequestParameterField, BadRequestIdParameter {
        when(userRepository.findByPictureLinkContaining(eq("pic"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user1)));

        List<User> result = userService.findAll(0, 10, null, null, null, "pic", null);

        assertThat(result).containsExactly(user1);
    }

    @Test
    void findAll_filterById_validLong_returnsMatch() throws BadRequestParameterField, BadRequestIdParameter {
        when(userRepository.findById(eq(42L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user1)));

        List<User> result = userService.findAll(0, 10, "42", null, null, null, null);

        assertThat(result).containsExactly(user1);
    }

    @Test
    void findAll_filterById_invalidLong_throwsBadRequestIdParameter() {
        assertThatThrownBy(() -> userService.findAll(0, 10, "not-a-number", null, null, null, null))
                .isInstanceOf(BadRequestIdParameter.class);
    }

    @Test
    void findAll_multipleFilters_throwsBadRequest() {
        assertThatThrownBy(() -> userService.findAll(0, 10, null, "Alice", "alice", null, null))
                .isInstanceOf(BadRequestParameterField.class);
    }

    // ---- findById ----

    @Test
    void findById_existingStringId_returnsUser() throws UserNotFoundException {
        when(userRepository.findById("1")).thenReturn(Optional.of(user1));

        User result = userService.findById("1");

        assertThat(result).isEqualTo(user1);
    }

    @Test
    void findById_notFound_throwsException() {
        when(userRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById("bad"))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ---- findByVideo ----

    @Test
    void findByVideo_existingVideo_returnsAuthors() throws VideoNotFoundException {
        when(videoRepository.findById("vid-1")).thenReturn(Optional.of(video));

        List<User> result = userService.findByVideo("vid-1");

        assertThat(result).containsExactly(user1);
    }

    @Test
    void findByVideo_notFound_throwsException() {
        when(videoRepository.findById("bad-vid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByVideo("bad-vid"))
                .isInstanceOf(VideoNotFoundException.class);
    }

    // ---- update ----

    @Test
    void update_existingUser_updatesFields() throws UserNotFoundException {
        when(userRepository.findById("1")).thenReturn(Optional.of(user1));

        User updated = new User("Alice Updated", "http://new.com", "http://new.com/pic.jpg");
        userService.update("1", updated);

        assertThat(user1.getName()).isEqualTo("Alice Updated");
        assertThat(user1.getUser_link()).isEqualTo("http://new.com");
        assertThat(user1.getPicture_link()).isEqualTo("http://new.com/pic.jpg");
        verify(userRepository).save(user1);
    }

    @Test
    void update_notFound_throwsException() {
        when(userRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update("bad", user1))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ---- delete ----

    @Test
    void delete_existingUser_deletesLinkedComment() throws UserNotFoundException {
        when(userRepository.findById("1")).thenReturn(Optional.of(user1));
        when(commentRepository.findByAuthor(user1)).thenReturn(comment);

        userService.delete("1");

        verify(commentRepository).delete(comment);
    }

    @Test
    void delete_notFound_throwsException() {
        when(userRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete("bad"))
                .isInstanceOf(UserNotFoundException.class);
    }
}
