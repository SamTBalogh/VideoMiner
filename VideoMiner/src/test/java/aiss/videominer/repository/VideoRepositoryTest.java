package aiss.videominer.repository;

import aiss.videominer.model.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VideoRepositoryTest {

    @Autowired
    VideoRepository videoRepository;

    @BeforeEach
    void setUp() {
        videoRepository.saveAll(List.of(
                new Video("vid-1", "Java Tutorial", "Learn Java", "2024-01-15"),
                new Video("vid-2", "Spring Boot Guide", "Spring overview", "2024-02-20"),
                new Video("vid-3", "Java Advanced", "Advanced Java topics", "2024-03-10")
        ));
    }

    @Test
    void findByName_exactName_returnsMatch() {
        List<Video> result = videoRepository.findByName("Java Tutorial", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("vid-1");
    }

    @Test
    void findById_existingId_returnsSingle() {
        List<Video> result = videoRepository.findById("vid-2", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Spring Boot Guide");
    }

    @Test
    void findByDescriptionContaining_partialDesc_returnsMatches() {
        List<Video> result = videoRepository.findByDescriptionContaining("Java", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(2);
    }

    @Test
    void findByReleaseTimeContaining_partialDate_returnsMatches() {
        List<Video> result = videoRepository.findByReleaseTimeContaining("2024-02", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("vid-2");
    }

    @Test
    void findAll_paged_returnsPagedResults() {
        List<Video> result = videoRepository.findAll(PageRequest.of(0, 2)).getContent();
        assertThat(result).hasSize(2);
    }
}
