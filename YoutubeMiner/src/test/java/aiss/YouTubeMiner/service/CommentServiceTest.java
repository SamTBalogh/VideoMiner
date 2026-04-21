package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.CommentNotFoundException;
import aiss.YouTubeMiner.model.VideoMinerModel.Comment;
import aiss.YouTubeMiner.model.YouTubeModel.comment.CommentSearch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    CommentService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "token", "test-token");
        ReflectionTestUtils.setField(service, "uri", "https://youtube.googleapis.com/youtube/v3");
    }

    @Test
    @DisplayName("findCommentsByVideoIdMax returns empty list when comments are disabled (403)")
    void findCommentsByVideoIdMax_forbidden() throws CommentNotFoundException {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(CommentSearch.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Forbidden", null, null, null));

        List<Comment> comments = service.findCommentsByVideoIdMax("v1", 10);
        assertNotNull(comments);
        assertEquals(0, comments.size());
    }

    @Test
    @DisplayName("findCommentsByVideoIdMax returns list of comments")
    void findCommentsByVideoIdMax_success() throws CommentNotFoundException {
        CommentSearch commentSearch = new CommentSearch();
        commentSearch.setItems(List.of());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(CommentSearch.class)))
                .thenReturn(ResponseEntity.ok(commentSearch));

        List<Comment> comments = service.findCommentsByVideoIdMax("v1", 10);
        assertNotNull(comments);
    }

    @Test
    @DisplayName("findCommentsByVideoIdMax throws CommentNotFoundException on 404")
    void findCommentsByVideoIdMax_notFound() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(CommentSearch.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThrows(CommentNotFoundException.class, () -> service.findCommentsByVideoIdMax("unknown", 5));
    }
}
