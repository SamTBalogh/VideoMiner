package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.CommentNotFoundException;
import aiss.YouTubeMiner.model.VideoMinerModel.Comment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class CommentServiceTest {

    @Autowired
    CommentService service;

    @Test
    @DisplayName("Get comments from a video id")
    void findCommentsFromVideoId() throws CommentNotFoundException {
        List<Comment> comment = service.findCommentsByVideoId("_VB39Jo8mAQ");
        assertNotNull(comment);
        System.out.println(comment);
    }

    @Test
    @DisplayName("Get comments from a video with comments disabled")
    void findCommentsFromVideoIdDisabled() throws CommentNotFoundException {
        List<Comment> comment = service.findCommentsByVideoId("KZ613lCnoJ0");
        assertNotNull(comment);
        System.out.println(comment);
    }

    @Test
    @DisplayName("Get comments from an invalid video Id")
    void findCommentsFromInvalidVideoId() {
        assertThrows(CommentNotFoundException.class, () -> {service.findCommentsByVideoId("Wololo");});
    }

    @Test
    @DisplayName("Get a certain number of comments from a video")
    void findMaxCommentsFromVideoId() throws CommentNotFoundException {
        List<Comment> comment = service.findCommentsByVideoIdMax("mKIhHNznt4s", 10);
        assertNotNull(comment);
        Assertions.assertEquals(10, comment.size());
        System.out.println(comment);
        System.out.println(comment.size());
    }

    @Test
    @DisplayName("Get comments from a video with comments disabled")
    void findMaxCommentsFromVideoIdDisabled() throws CommentNotFoundException{
        List<Comment> comment = service.findCommentsByVideoIdMax("KZ613lCnoJ0", 4);
        assertNotNull(comment);
        Assertions.assertEquals(0, comment.size());
        System.out.println(comment);
        System.out.println(comment.size());
    }

    @Test
    @DisplayName("Get comments from an invalid video Id")
    void findMaxCommentsFromInvalidVideoId() {
        assertThrows(CommentNotFoundException.class, () -> {service.findCommentsByVideoIdMax("Wololo", 10);});
    }
}
