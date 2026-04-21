package aiss.videominer.repository;

import aiss.videominer.model.Comment;
import aiss.videominer.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    UserRepository userRepository;

    private User alice;

    @BeforeEach
    void setUp() {
        alice = new User("Alice", "http://alice.com", "http://alice.com/pic.jpg");
        alice = userRepository.save(alice);

        User bob = new User("Bob", "http://bob.com", "http://bob.com/pic.jpg");
        bob = userRepository.save(bob);

        User charlie = new User("Charlie", "http://charlie.com", "http://charlie.com/pic.jpg");
        charlie = userRepository.save(charlie);

        commentRepository.saveAll(List.of(
                new Comment("com-1", "Hello world", "2024-01-01", alice),
                new Comment("com-2", "Great post!", "2024-01-02", bob),
                new Comment("com-3", "Hello again", "2024-01-03", charlie)
        ));
    }

    @Test
    void findById_existingId_returnsSingle() {
        List<Comment> result = commentRepository.findById("com-1", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getText()).isEqualTo("Hello world");
    }

    @Test
    void findByTextContaining_partialText_returnsMatches() {
        List<Comment> result = commentRepository.findByTextContaining("Hello", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(2);
    }

    @Test
    void findByCreatedOnContaining_partialDate_returnsMatches() {
        List<Comment> result = commentRepository.findByCreatedOnContaining("2024-01", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(3);
    }

    @Test
    void findByAuthor_existingAuthor_returnsComment() {
        Comment result = commentRepository.findByAuthor(alice);
        assertThat(result).isNotNull();
        assertThat(result.getAuthor().getName()).isEqualTo("Alice");
    }

    @Test
    void findAll_paged_returnsPagedResults() {
        List<Comment> result = commentRepository.findAll(PageRequest.of(0, 2)).getContent();
        assertThat(result).hasSize(2);
    }
}
