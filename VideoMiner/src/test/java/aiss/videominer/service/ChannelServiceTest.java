package aiss.videominer.service;

import aiss.videominer.exception.BadRequestParameterField;
import aiss.videominer.exception.ChannelNotFoundException;
import aiss.videominer.exception.IdCannotBeNull;
import aiss.videominer.model.Channel;
import aiss.videominer.repository.ChannelRepository;
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
class ChannelServiceTest {

    @Mock
    ChannelRepository channelRepository;

    @InjectMocks
    ChannelService channelService;

    private Channel channel1;
    private Channel channel2;

    @BeforeEach
    void setUp() {
        channel1 = new Channel("ch-1", "Channel One", "Description 1", "2024-01-01");
        channel2 = new Channel("ch-2", "Channel Two", "Description 2", "2024-02-01");
    }

    // ---- findAll ----

    @Test
    void findAll_noFilter_returnsAll() throws BadRequestParameterField {
        when(channelRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(channel1, channel2)));

        List<Channel> result = channelService.findAll(0, 10, null, null, null, null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void findAll_filterById_returnsMatch() throws BadRequestParameterField {
        when(channelRepository.findByIdContaining(eq("ch-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(channel1)));

        List<Channel> result = channelService.findAll(0, 10, "ch-1", null, null, null, null);

        assertThat(result).containsExactly(channel1);
    }

    @Test
    void findAll_filterByName_returnsMatch() throws BadRequestParameterField {
        when(channelRepository.findByNameContaining(eq("Channel One"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(channel1)));

        List<Channel> result = channelService.findAll(0, 10, null, "Channel One", null, null, null);

        assertThat(result).containsExactly(channel1);
    }

    @Test
    void findAll_filterByDescription_returnsMatch() throws BadRequestParameterField {
        when(channelRepository.findByDescriptionContaining(eq("Description 1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(channel1)));

        List<Channel> result = channelService.findAll(0, 10, null, null, "Description 1", null, null);

        assertThat(result).containsExactly(channel1);
    }

    @Test
    void findAll_filterByCreatedTime_returnsMatch() throws BadRequestParameterField {
        when(channelRepository.findByCreatedTimeContaining(eq("2024-01-01"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(channel1)));

        List<Channel> result = channelService.findAll(0, 10, null, null, null, "2024-01-01", null);

        assertThat(result).containsExactly(channel1);
    }

    @Test
    void findAll_multipleFilters_throwsBadRequest() {
        assertThatThrownBy(() -> channelService.findAll(0, 10, "ch-1", "Channel One", null, null, null))
                .isInstanceOf(BadRequestParameterField.class);
    }

    // ---- findById ----

    @Test
    void findById_existingId_returnsChannel() throws ChannelNotFoundException {
        when(channelRepository.findById("ch-1")).thenReturn(Optional.of(channel1));

        Channel result = channelService.findById("ch-1");

        assertThat(result).isEqualTo(channel1);
    }

    @Test
    void findById_notFound_throwsException() {
        when(channelRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.findById("bad-id"))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    // ---- create ----

    @Test
    void create_validChannel_saves() throws IdCannotBeNull {
        when(channelRepository.save(channel1)).thenReturn(channel1);

        Channel result = channelService.create(channel1);

        assertThat(result).isEqualTo(channel1);
        verify(channelRepository).save(channel1);
    }

    @Test
    void create_nullId_throwsIdCannotBeNull() {
        Channel noId = new Channel(null, "Name", "desc", "2024-01-01");

        assertThatThrownBy(() -> channelService.create(noId))
                .isInstanceOf(IdCannotBeNull.class);
    }

    // ---- update ----

    @Test
    void update_existingChannel_updatesNameAndDescription() throws ChannelNotFoundException {
        when(channelRepository.findById("ch-1")).thenReturn(Optional.of(channel1));

        Channel updated = new Channel("ch-1", "New Name", "New Desc", "2024-01-01");
        channelService.update("ch-1", updated);

        assertThat(channel1.getName()).isEqualTo("New Name");
        assertThat(channel1.getDescription()).isEqualTo("New Desc");
        verify(channelRepository).save(channel1);
    }

    @Test
    void update_notFound_throwsException() {
        when(channelRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.update("bad-id", channel1))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    // ---- delete ----

    @Test
    void delete_existingChannel_deletes() throws ChannelNotFoundException {
        when(channelRepository.existsById("ch-1")).thenReturn(true);

        channelService.delete("ch-1");

        verify(channelRepository).deleteById("ch-1");
    }

    @Test
    void delete_notFound_throwsException() {
        when(channelRepository.existsById("bad-id")).thenReturn(false);

        assertThatThrownBy(() -> channelService.delete("bad-id"))
                .isInstanceOf(ChannelNotFoundException.class);
    }
}
