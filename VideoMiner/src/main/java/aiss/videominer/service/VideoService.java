package aiss.videominer.service;

import aiss.videominer.exception.BadRequestParameterField;
import aiss.videominer.exception.ChannelNotFoundException;
import aiss.videominer.exception.IdCannotBeNull;
import aiss.videominer.exception.VideoNotFoundException;
import aiss.videominer.model.Channel;
import aiss.videominer.model.Video;
import aiss.videominer.repository.ChannelRepository;
import aiss.videominer.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class VideoService {

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    ChannelRepository channelRepository;

    public List<Video> findAll(int page, int size, String id, String name,
                               String description, String releaseTime, String order)
            throws BadRequestParameterField {

        Pageable paging = PageableHelper.build(page, size, order);

        if (Stream.of(id, name, description, releaseTime).filter(Objects::nonNull).count() > 1)
            throw new BadRequestParameterField();

        if (id != null) return videoRepository.findById(id, paging).getContent();
        if (name != null) return videoRepository.findByName(name, paging).getContent();
        if (description != null) return videoRepository.findByDescriptionContaining(description, paging).getContent();
        if (releaseTime != null) return videoRepository.findByReleaseTimeContaining(releaseTime, paging).getContent();
        return videoRepository.findAll(paging).getContent();
    }

    public Video findById(String id) throws VideoNotFoundException {
        return videoRepository.findById(id).orElseThrow(VideoNotFoundException::new);
    }

    public List<Video> findByChannel(String channelId) throws ChannelNotFoundException {
        Channel channel = channelRepository.findById(channelId).orElseThrow(ChannelNotFoundException::new);
        return new ArrayList<>(channel.getVideos());
    }

    public List<Video> create(String channelId, Video video) throws ChannelNotFoundException, IdCannotBeNull {
        if (video.getId() == null) throw new IdCannotBeNull();
        Channel channel = channelRepository.findById(channelId).orElseThrow(ChannelNotFoundException::new);
        channel.getVideos().add(video);
        channelRepository.save(channel);
        return channel.getVideos();
    }

    public void update(String id, Video updated) throws VideoNotFoundException {
        Video video = videoRepository.findById(id).orElseThrow(VideoNotFoundException::new);
        if (updated.getName() != null) video.setName(updated.getName());
        if (updated.getDescription() != null) video.setDescription(updated.getDescription());
        videoRepository.save(video);
    }

    public void delete(String id) throws VideoNotFoundException {
        if (!videoRepository.existsById(id)) throw new VideoNotFoundException();
        videoRepository.deleteById(id);
    }
}
