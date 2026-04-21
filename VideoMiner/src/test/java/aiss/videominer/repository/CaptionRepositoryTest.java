package aiss.videominer.repository;

import aiss.videominer.model.Caption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CaptionRepositoryTest {

    @Autowired
    CaptionRepository captionRepository;

    @BeforeEach
    void setUp() {
        captionRepository.saveAll(List.of(
                new Caption("cap-1", "en", "English"),
                new Caption("cap-2", "es", "Spanish"),
                new Caption("cap-3", "en", "British English")
        ));
    }

    @Test
    void findByName_existingName_returnsMatch() {
        List<Caption> result = captionRepository.findByName("English", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("cap-1");
    }

    @Test
    void findByLanguage_existingLanguage_returnsAllMatches() {
        List<Caption> result = captionRepository.findByLanguage("en", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(2);
    }

    @Test
    void findById_existingId_returnsSingle() {
        List<Caption> result = captionRepository.findById("cap-2", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Spanish");
    }

    @Test
    void findAll_paged_returnsPagedResults() {
        List<Caption> result = captionRepository.findAll(PageRequest.of(0, 2)).getContent();
        assertThat(result).hasSize(2);
    }
}
