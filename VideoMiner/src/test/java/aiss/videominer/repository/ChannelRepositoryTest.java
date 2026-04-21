package aiss.videominer.repository;

import aiss.videominer.model.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ChannelRepositoryTest {

    @Autowired
    ChannelRepository channelRepository;

    @BeforeEach
    void setUp() {
        channelRepository.saveAll(List.of(
                new Channel("ch-1", "Tech News", "All about tech", "2024-01-01"),
                new Channel("ch-2", "Gaming Hub", "Gaming content", "2024-02-01"),
                new Channel("ch-3", "Tech Reviews", "Latest reviews", "2024-03-01")
        ));
    }

    @Test
    void findByIdContaining_partialId_returnsMatches() {
        List<Channel> result = channelRepository.findByIdContaining("ch-", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(3);
    }

    @Test
    void findByNameContaining_partialName_returnsMatches() {
        List<Channel> result = channelRepository.findByNameContaining("Tech", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(2);
    }

    @Test
    void findByDescriptionContaining_partialDesc_returnsMatches() {
        List<Channel> result = channelRepository.findByDescriptionContaining("content", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("ch-2");
    }

    @Test
    void findByCreatedTimeContaining_partialDate_returnsMatches() {
        List<Channel> result = channelRepository.findByCreatedTimeContaining("2024", PageRequest.of(0, 10)).getContent();
        assertThat(result).hasSize(3);
    }

    @Test
    void findAll_paged_returnsPagedResults() {
        List<Channel> result = channelRepository.findAll(PageRequest.of(0, 2)).getContent();
        assertThat(result).hasSize(2);
    }
}
