package aiss.videominer.repository;

import aiss.videominer.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.saveAll(List.of(
                new User("Alice", "http://alice.com", "http://alice.com/pic.jpg"),
                new User("Bob", "http://bob.org", "http://bob.org/avatar.png"),
                new User("Charlie", "http://charlie.com", "http://charlie.com/photo.jpg")
        ));
    }

    @Test
    void findByName_exactName_returnsMatch() {
        List<User> result = userRepository.findByName("Alice", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void findByUserLinkContaining_partialLink_returnsMatches() {
        List<User> result = userRepository.findByUserLinkContaining(".com", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(2);
    }

    @Test
    void findByPictureLinkContaining_partialLink_returnsMatches() {
        List<User> result = userRepository.findByPictureLinkContaining("pic.jpg", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void findById_numericId_returnsSingle() {
        User saved = userRepository.findAll().get(0);
        List<User> result = userRepository.findById(saved.getId(), PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(1);
    }

    @Test
    void findAll_paged_returnsPagedResults() {
        List<User> result = userRepository.findAll(PageRequest.of(0, 2)).getContent();
        assertThat(result).hasSize(2);
    }
}
