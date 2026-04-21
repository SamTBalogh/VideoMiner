package aiss.videominer.service;

import aiss.videominer.exception.BadRequestParameterField;
import aiss.videominer.exception.ChannelNotFoundException;
import aiss.videominer.exception.IdCannotBeNull;
import aiss.videominer.exception.VideoNotFoundException;
import aiss.videominer.model.Channel;
import aiss.videominer.model.Video;
import aiss.videominer.repository.ChannelRepository;
import aiss.videominer.repository.VideoRepository;
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
class VideoServiceTest {

    @Mock
    VideoRepository videoRepository;

    @Mock
    ChannelRepository channelRepository;

    @InjectMocks
    VideoService videoService;

    private Video video1;
    private Video video2;
    private Channel channel;

    @BeforeEach
    void setUp() {
        video1 = new Video("vid-1", "Video One", "Desc 1", "2024-01-01");
        video2 = new Video("vid-2", "Video Two", "Desc 2", "2024-02-01");

        channel = new Channel("ch-1", "Channel One", "desc", "2024-01-01");
        channel.getVideos().add(video1);
    }

    // ---- findAll ----

    @Test
    void findAll_noFilter_returnsAll() throws BadRequestParameterField {
        when(videoRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(video1, video2)));

        List<Video> result = videoService.findAll(0, 10, null, null, null, null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void findAll_filterById_returnsMatch() throws BadRequestParameterField {
        when(videoRepository.findById(eq("vid-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(video1)));

        List<Video> result = videoService.findAll(0, 10, "vid-1", null, null, null, null);

        assertThat(result).containsExactly(video1);
    }

    @Test
    void findAll_filterByName_returnsMatch() throws BadRequestParameterField {
        when(videoRepository.findByName(eq("Video One"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(video1)));

        List<Video> result = videoService.findAll(0, 10, null, "Video One", null, null, null);

        assertThat(result).containsExactly(video1);
    }

    @Test
    void findAll_filterByDescription_returnsMatch() throws BadRequestParameterField {
        when(videoRepository.findByDescriptionContaining(eq("Desc 1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(video1)));

        List<Video> result = videoService.findAll(0, 10, null, null, "Desc 1", null, null);

        assertThat(result).containsExactly(video1);
    }

    @Test
    void findAll_filterByReleaseTime_returnsMatch() throws BadRequestParameterField {
        when(videoRepository.findByReleaseTimeContaining(eq("2024-01-01"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(video1)));

        List<Video> result = videoService.findAll(0, 10, null, null, null, "2024-01-01", null);

        assertThat(result).containsExactly(video1);
    }

    @Test
    void findAll_multipleFilters_throwsBadRequest() {
        assertThatThrownBy(() -> videoService.findAll(0, 10, "vid-1", "Video One", null, null, null))
                .isInstanceOf(BadRequestParameterField.class);
    }

    // ---- findById ----

    @Test
    void findById_existingId_returnsVideo() throws VideoNotFoundException {
        when(videoRepository.findById("vid-1")).thenReturn(Optional.of(video1));

        Video result = videoService.findById("vid-1");

        assertThat(result).isEqualTo(video1);
    }

    @Test
    void findById_notFound_throwsException() {
        when(videoRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoService.findById("bad-id"))
                .isInstanceOf(VideoNotFoundException.class);
    }

    // ---- findByChannel ----

    @Test
    void findByChannel_existingChannel_returnsVideos() throws ChannelNotFoundException {
        when(channelRepository.findById("ch-1")).thenReturn(Optional.of(channel));

        List<Video> result = videoService.findByChannel("ch-1");

        assertThat(result).containsExactly(video1);
    }

    @Test
    void findByChannel_notFound_throwsException() {
        when(channelRepository.findById("bad-ch")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoService.findByChannel("bad-ch"))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    // ---- create ----

    @Test
    void create_validVideo_addsToChannel() throws ChannelNotFoundException, IdCannotBeNull {
        when(channelRepository.findById("ch-1")).thenReturn(Optional.of(channel));
        when(channelRepository.save(any(Channel.class))).thenReturn(channel);

        List<Video> result = videoService.create("ch-1", video2);

        assertThat(result).contains(video2);
        verify(channelRepository).save(channel);
    }

    @Test
    void create_nullId_throwsIdCannotBeNull() {
        Video noId = new Video(null, "Name", "desc", "2024-01-01");

        assertThatThrownBy(() -> videoService.create("ch-1", noId))
                .isInstanceOf(IdCannotBeNull.class);
    }

    @Test
    void create_channelNotFound_throwsException() {
        when(channelRepository.findById("bad-ch")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoService.create("bad-ch", video2))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    // ---- update ----

    @Test
    void update_existingVideo_updatesFields() throws VideoNotFoundException {
        when(videoRepository.findById("vid-1")).thenReturn(Optional.of(video1));

        Video updated = new Video("vid-1", "Updated Name", "Updated Desc", "2024-01-01");
        videoService.update("vid-1", updated);

        assertThat(video1.getName()).isEqualTo("Updated Name");
        assertThat(video1.getDescription()).isEqualTo("Updated Desc");
        verify(videoRepository).save(video1);
    }

    @Test
    void update_notFound_throwsException() {
        when(videoRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoService.update("bad-id", video1))
                .isInstanceOf(VideoNotFoundException.class);
    }

    // ---- delete ----

    @Test
    void delete_existingVideo_deletes() throws VideoNotFoundException {
        when(videoRepository.existsById("vid-1")).thenReturn(true);

        videoService.delete("vid-1");

        verify(videoRepository).deleteById("vid-1");
    }

    @Test
    void delete_notFound_throwsException() {
        when(videoRepository.existsById("bad-id")).thenReturn(false);

        assertThatThrownBy(() -> videoService.delete("bad-id"))
                .isInstanceOf(VideoNotFoundException.class);
    }
}
