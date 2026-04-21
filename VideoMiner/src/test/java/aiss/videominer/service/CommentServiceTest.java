package aiss.videominer.service;

import aiss.videominer.exception.BadRequestParameterField;
import aiss.videominer.exception.CommentNotFoundException;
import aiss.videominer.exception.IdCannotBeNull;
import aiss.videominer.exception.VideoNotFoundException;
import aiss.videominer.model.Comment;
import aiss.videominer.model.User;
import aiss.videominer.model.Video;
import aiss.videominer.repository.CommentRepository;
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
class CommentServiceTest {

    @Mock
    CommentRepository commentRepository;

    @Mock
    VideoRepository videoRepository;

    @InjectMocks
    CommentService commentService;

    private User author;
    private Comment comment1;
    private Comment comment2;
    private Video video;

    @BeforeEach
    void setUp() {
        author = new User("Alice", "http://alice.com", "http://alice.com/pic.jpg");
        comment1 = new Comment("com-1", "Hello world", "2024-01-01", author);
        comment2 = new Comment("com-2", "Second comment", "2024-01-02", author);

        video = new Video("vid-1", "Test Video", "desc", "2024-01-01");
        video.getComments().add(comment1);
    }

    // ---- findAll ----

    @Test
    void findAll_noFilter_returnsAll() throws BadRequestParameterField {
        when(commentRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comment1, comment2)));

        List<Comment> result = commentService.findAll(0, 10, null, null, null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void findAll_filterById_returnsMatch() throws BadRequestParameterField {
        when(commentRepository.findById(eq("com-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comment1)));

        List<Comment> result = commentService.findAll(0, 10, "com-1", null, null, null);

        assertThat(result).containsExactly(comment1);
    }

    @Test
    void findAll_filterByText_returnsMatch() throws BadRequestParameterField {
        when(commentRepository.findByTextContaining(eq("Hello"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comment1)));

        List<Comment> result = commentService.findAll(0, 10, null, "Hello", null, null);

        assertThat(result).containsExactly(comment1);
    }

    @Test
    void findAll_filterByCreatedOn_returnsMatch() throws BadRequestParameterField {
        when(commentRepository.findByCreatedOnContaining(eq("2024-01-01"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comment1)));

        List<Comment> result = commentService.findAll(0, 10, null, null, "2024-01-01", null);

        assertThat(result).containsExactly(comment1);
    }

    @Test
    void findAll_multipleFilters_throwsBadRequest() {
        assertThatThrownBy(() -> commentService.findAll(0, 10, "com-1", "Hello", null, null))
                .isInstanceOf(BadRequestParameterField.class);
    }

    // ---- findById ----

    @Test
    void findById_existingId_returnsComment() throws CommentNotFoundException {
        when(commentRepository.findById("com-1")).thenReturn(Optional.of(comment1));

        Comment result = commentService.findById("com-1");

        assertThat(result).isEqualTo(comment1);
    }

    @Test
    void findById_notFound_throwsException() {
        when(commentRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.findById("bad-id"))
                .isInstanceOf(CommentNotFoundException.class);
    }

    // ---- findByVideo ----

    @Test
    void findByVideo_existingVideo_returnsComments() throws VideoNotFoundException {
        when(videoRepository.findById("vid-1")).thenReturn(Optional.of(video));

        List<Comment> result = commentService.findByVideo("vid-1");

        assertThat(result).containsExactly(comment1);
    }

    @Test
    void findByVideo_notFound_throwsException() {
        when(videoRepository.findById("bad-vid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.findByVideo("bad-vid"))
                .isInstanceOf(VideoNotFoundException.class);
    }

    // ---- create ----

    @Test
    void create_validComment_addsToVideo() throws VideoNotFoundException, IdCannotBeNull {
        when(videoRepository.findById("vid-1")).thenReturn(Optional.of(video));
        when(videoRepository.save(any(Video.class))).thenReturn(video);

        List<Comment> result = commentService.create("vid-1", comment2);

        assertThat(result).contains(comment2);
        verify(videoRepository).save(video);
    }

    @Test
    void create_nullId_throwsIdCannotBeNull() {
        Comment noId = new Comment(null, "text", "2024-01-01", author);

        assertThatThrownBy(() -> commentService.create("vid-1", noId))
                .isInstanceOf(IdCannotBeNull.class);
    }

    @Test
    void create_videoNotFound_throwsException() {
        when(videoRepository.findById("bad-vid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create("bad-vid", comment2))
                .isInstanceOf(VideoNotFoundException.class);
    }

    // ---- update ----

    @Test
    void update_existingComment_updatesFields() throws CommentNotFoundException {
        when(commentRepository.findById("com-1")).thenReturn(Optional.of(comment1));

        Comment updated = new Comment("com-1", "Updated text", "2024-06-01", author);
        commentService.update("com-1", updated);

        assertThat(comment1.getText()).isEqualTo("Updated text");
        assertThat(comment1.getCreatedOn()).isEqualTo("2024-06-01");
        verify(commentRepository).save(comment1);
    }

    @Test
    void update_notFound_throwsException() {
        when(commentRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.update("bad-id", comment1))
                .isInstanceOf(CommentNotFoundException.class);
    }

    // ---- delete ----

    @Test
    void delete_existingComment_deletes() throws CommentNotFoundException {
        when(commentRepository.findById("com-1")).thenReturn(Optional.of(comment1));

        commentService.delete("com-1");

        verify(commentRepository).deleteById("com-1");
    }

    @Test
    void delete_notFound_throwsException() {
        when(commentRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.delete("bad-id"))
                .isInstanceOf(CommentNotFoundException.class);
    }
}
