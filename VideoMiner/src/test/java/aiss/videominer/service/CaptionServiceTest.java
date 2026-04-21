package aiss.videominer.service;

import aiss.videominer.exception.BadRequestParameterField;
import aiss.videominer.exception.CaptionNotFoundException;
import aiss.videominer.exception.IdCannotBeNull;
import aiss.videominer.exception.VideoNotFoundException;
import aiss.videominer.model.Caption;
import aiss.videominer.model.Video;
import aiss.videominer.repository.CaptionRepository;
import aiss.videominer.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaptionServiceTest {

    @Mock
    CaptionRepository captionRepository;

    @Mock
    VideoRepository videoRepository;

    @InjectMocks
    CaptionService captionService;

    private Caption caption1;
    private Caption caption2;
    private Video video;

    @BeforeEach
    void setUp() {
        caption1 = new Caption("cap-1", "en", "English");
        caption2 = new Caption("cap-2", "es", "Spanish");

        video = new Video("vid-1", "Test Video", "desc", "2024-01-01");
        video.getCaptions().add(caption1);
    }

    // ---- findAll ----

    @Test
    void findAll_noFilter_returnsAll() throws BadRequestParameterField {
        when(captionRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(caption1, caption2)));

        List<Caption> result = captionService.findAll(0, 10, null, null, null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void findAll_filterById_returnsByIdPage() throws BadRequestParameterField {
        when(captionRepository.findById(eq("cap-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(caption1)));

        List<Caption> result = captionService.findAll(0, 10, "cap-1", null, null, null);

        assertThat(result).containsExactly(caption1);
    }

    @Test
    void findAll_filterByName_returnsByName() throws BadRequestParameterField {
        when(captionRepository.findByName(eq("English"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(caption1)));

        List<Caption> result = captionService.findAll(0, 10, null, "English", null, null);

        assertThat(result).containsExactly(caption1);
    }

    @Test
    void findAll_filterByLanguage_returnsByLanguage() throws BadRequestParameterField {
        when(captionRepository.findByLanguage(eq("en"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(caption1)));

        List<Caption> result = captionService.findAll(0, 10, null, null, "en", null);

        assertThat(result).containsExactly(caption1);
    }

    @Test
    void findAll_multipleFilters_throwsBadRequest() {
        assertThatThrownBy(() -> captionService.findAll(0, 10, "cap-1", "English", null, null))
                .isInstanceOf(BadRequestParameterField.class);
    }

    @Test
    void findAll_orderAscending_usesSort() throws BadRequestParameterField {
        when(captionRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(caption1)));

        List<Caption> result = captionService.findAll(0, 10, null, null, null, "name");

        assertThat(result).isNotNull();
    }

    @Test
    void findAll_orderDescending_usesSort() throws BadRequestParameterField {
        when(captionRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(caption2)));

        List<Caption> result = captionService.findAll(0, 10, null, null, null, "-name");

        assertThat(result).isNotNull();
    }

    // ---- findById ----

    @Test
    void findById_existingId_returnsCaption() throws CaptionNotFoundException {
        when(captionRepository.findById("cap-1")).thenReturn(Optional.of(caption1));

        Caption result = captionService.findById("cap-1");

        assertThat(result).isEqualTo(caption1);
    }

    @Test
    void findById_notFound_throwsException() {
        when(captionRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> captionService.findById("bad-id"))
                .isInstanceOf(CaptionNotFoundException.class);
    }

    // ---- findByVideo ----

    @Test
    void findByVideo_existingVideo_returnsCaptions() throws VideoNotFoundException {
        when(videoRepository.findById("vid-1")).thenReturn(Optional.of(video));

        List<Caption> result = captionService.findByVideo("vid-1");

        assertThat(result).containsExactly(caption1);
    }

    @Test
    void findByVideo_videoNotFound_throwsException() {
        when(videoRepository.findById("bad-vid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> captionService.findByVideo("bad-vid"))
                .isInstanceOf(VideoNotFoundException.class);
    }

    // ---- create ----

    @Test
    void create_validCaption_addsToVideo() throws VideoNotFoundException, IdCannotBeNull {
        when(videoRepository.findById("vid-1")).thenReturn(Optional.of(video));
        when(videoRepository.save(any(Video.class))).thenReturn(video);

        List<Caption> result = captionService.create("vid-1", caption2);

        assertThat(result).contains(caption2);
        verify(videoRepository).save(video);
    }

    @Test
    void create_nullId_throwsIdCannotBeNull() {
        Caption noId = new Caption(null, "fr", "French");

        assertThatThrownBy(() -> captionService.create("vid-1", noId))
                .isInstanceOf(IdCannotBeNull.class);
    }

    @Test
    void create_videoNotFound_throwsException() {
        when(videoRepository.findById("bad-vid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> captionService.create("bad-vid", caption2))
                .isInstanceOf(VideoNotFoundException.class);
    }

    // ---- update ----

    @Test
    void update_existingCaption_updatesFields() throws CaptionNotFoundException {
        when(captionRepository.findById("cap-1")).thenReturn(Optional.of(caption1));

        Caption updated = new Caption("cap-1", "fr", "French");
        captionService.update("cap-1", updated);

        assertThat(caption1.getLanguage()).isEqualTo("fr");
        assertThat(caption1.getName()).isEqualTo("French");
        verify(captionRepository).save(caption1);
    }

    @Test
    void update_notFound_throwsException() {
        when(captionRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> captionService.update("bad-id", caption1))
                .isInstanceOf(CaptionNotFoundException.class);
    }

    // ---- delete ----

    @Test
    void delete_existingCaption_deletes() throws CaptionNotFoundException {
        when(captionRepository.existsById("cap-1")).thenReturn(true);

        captionService.delete("cap-1");

        verify(captionRepository).deleteById("cap-1");
    }

    @Test
    void delete_notFound_throwsException() {
        when(captionRepository.existsById("bad-id")).thenReturn(false);

        assertThatThrownBy(() -> captionService.delete("bad-id"))
                .isInstanceOf(CaptionNotFoundException.class);
    }
}
