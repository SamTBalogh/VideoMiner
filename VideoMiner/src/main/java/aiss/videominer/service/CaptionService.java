package aiss.videominer.service;

import aiss.videominer.exception.BadRequestParameterField;
import aiss.videominer.exception.CaptionNotFoundException;
import aiss.videominer.exception.IdCannotBeNull;
import aiss.videominer.exception.VideoNotFoundException;
import aiss.videominer.model.Caption;
import aiss.videominer.model.Video;
import aiss.videominer.repository.CaptionRepository;
import aiss.videominer.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class CaptionService {

    @Autowired
    CaptionRepository captionRepository;

    @Autowired
    VideoRepository videoRepository;

    public List<Caption> findAll(int page, int size, String id, String name,
                                 String language, String order)
            throws BadRequestParameterField {

        Pageable paging = PageableHelper.build(page, size, order);

        if (Stream.of(id, name, language).filter(Objects::nonNull).count() > 1)
            throw new BadRequestParameterField();

        if (id != null) return captionRepository.findById(id, paging).getContent();
        if (name != null) return captionRepository.findByName(name, paging).getContent();
        if (language != null) return captionRepository.findByLanguage(language, paging).getContent();
        return captionRepository.findAll(paging).getContent();
    }

    public Caption findById(String id) throws CaptionNotFoundException {
        return captionRepository.findById(id).orElseThrow(CaptionNotFoundException::new);
    }

    public List<Caption> findByVideo(String videoId) throws VideoNotFoundException {
        Video video = videoRepository.findById(videoId).orElseThrow(VideoNotFoundException::new);
        return video.getCaptions();
    }

    public List<Caption> create(String videoId, Caption caption) throws VideoNotFoundException, IdCannotBeNull {
        if (caption.getId() == null) throw new IdCannotBeNull();
        Video video = videoRepository.findById(videoId).orElseThrow(VideoNotFoundException::new);
        video.getCaptions().add(caption);
        videoRepository.save(video);
        return video.getCaptions();
    }

    public void update(String id, Caption updated) throws CaptionNotFoundException {
        Caption caption = captionRepository.findById(id).orElseThrow(CaptionNotFoundException::new);
        if (updated.getName() != null) caption.setName(updated.getName());
        if (updated.getLanguage() != null) caption.setLanguage(updated.getLanguage());
        captionRepository.save(caption);
    }

    public void delete(String id) throws CaptionNotFoundException {
        if (!captionRepository.existsById(id)) throw new CaptionNotFoundException();
        captionRepository.deleteById(id);
    }
}
