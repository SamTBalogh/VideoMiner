package aiss.vimeominer.service;

import aiss.vimeominer.exception.CommentsNotFoundException;
import aiss.vimeominer.model.VideoMiner.Comment;
import aiss.vimeominer.model.VimeoMiner.comment.VimeoComment;
import aiss.vimeominer.model.VimeoMiner.comment.VimeoCommentSearch;
import aiss.vimeominer.model.VimeoMiner.comment.VimeoUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    CommentService commentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(commentService, "token", "test-token");
        ReflectionTestUtils.setField(commentService, "uri", "https://api.vimeo.com");
    }

    private VimeoCommentSearch buildVimeoCommentSearch(String commentId) {
        aiss.vimeominer.model.VimeoMiner.comment.Picture picture = new aiss.vimeominer.model.VimeoMiner.comment.Picture();
        picture.setBaseLink("https://i.vimeocdn.com/portrait/user");
        VimeoUser user = new VimeoUser();
        user.setName("Test User");
        user.setLink("https://vimeo.com/user");
        user.setPictures(picture);
        VimeoComment c = new VimeoComment();
        c.setId("/videos/123/comments/" + commentId);
        c.setText("Test comment");
        c.setCreatedOn("2024-01-01T00:00:00+00:00");
        c.setUser(user);
        VimeoCommentSearch search = new VimeoCommentSearch();
        search.setComments(List.of(c));
        return search;
    }

    @Test
    @DisplayName("findCommentsByVideoId returns mapped comments")
    void findCommentsByVideoId_success() throws CommentsNotFoundException {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VimeoCommentSearch.class)))
                .thenReturn(ResponseEntity.ok(buildVimeoCommentSearch("1")));

        List<Comment> comments = commentService.findCommentsByVideoId("919411397");

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("Test comment", comments.get(0).getText());
    }

    @Test
    @DisplayName("findCommentsByVideoIdMaxComments returns mapped comments")
    void findCommentsByVideoIdMaxComments_success() throws CommentsNotFoundException {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VimeoCommentSearch.class)))
                .thenReturn(ResponseEntity.ok(buildVimeoCommentSearch("1")));

        List<Comment> comments = commentService.findCommentsByVideoIdMaxComments("919411397", 5);

        assertNotNull(comments);
        assertEquals(1, comments.size());
    }

    @Test
    @DisplayName("findCommentsByVideoId with invalid id throws CommentsNotFoundException")
    void findCommentsByVideoId_notFound() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VimeoCommentSearch.class)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThrows(CommentsNotFoundException.class, () -> commentService.findCommentsByVideoId("invalid"));
    }

    @Test
    @DisplayName("findCommentsByVideoIdMaxComments with invalid id throws CommentsNotFoundException")
    void findCommentsByVideoIdMaxComments_notFound() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(VimeoCommentSearch.class)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThrows(CommentsNotFoundException.class, () -> commentService.findCommentsByVideoIdMaxComments("invalid", 10));
    }
}
