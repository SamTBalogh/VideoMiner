package aiss.vimeominer.service;

import aiss.vimeominer.exception.CommentsNotFoundException;
import aiss.vimeominer.model.VideoMiner.Comment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CommentServiceTest {

    @Autowired
    CommentService service;

    @Test
    @DisplayName("Get Comments")
    void findCommentsByVideoId() throws CommentsNotFoundException {
        List<Comment> comments = service.findCommentsByVideoId("919411397");
        assertNotNull(comments);
        System.out.println(comments);
    }

    @Test
    @DisplayName("Get Comments Max Number")
    void findCommentsByVideoIdMaxComments() throws CommentsNotFoundException {
        List<Comment> comments = service.findCommentsByVideoIdMaxComments("941282766", 10);
        assertNotNull(comments);
        Assertions.assertEquals(10, comments.size());
        System.out.println("The number of comments in the list is "+comments.size());
        System.out.println(comments);
    }

    @Test
    @DisplayName("Get Comments Error 404")
    void findCommentsByVideoIdNotFound() throws CommentsNotFoundException {
        List<Comment> comments = service.findCommentsByVideoId("919411397");
        assertNotNull(comments);
        System.out.println(comments);
    }

    @Test
    @DisplayName("Get comments from an invalid video Id")
    void findCommentsFromInvalidVideoId() {
        assertThrows(CommentsNotFoundException.class, () -> {service.findCommentsByVideoId("Wololo");});
    }

    @Test
    @DisplayName("Get comments from an invalid video Id")
    void findMaxCommentsFromInvalidVideoId() {
        assertThrows(CommentsNotFoundException.class, () -> {service.findCommentsByVideoIdMaxComments("Wololo", 10);});
    }
}