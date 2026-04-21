package aiss.videominer.service;

import aiss.videominer.exception.BadRequestParameterField;
import aiss.videominer.exception.ChannelNotFoundException;
import aiss.videominer.exception.IdCannotBeNull;
import aiss.videominer.model.Channel;
import aiss.videominer.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class ChannelService {

    @Autowired
    ChannelRepository channelRepository;

    public List<Channel> findAll(int page, int size, String id, String name,
                                 String description, String createdTime, String order)
            throws BadRequestParameterField {

        Pageable paging = PageableHelper.build(page, size, order);

        if (Stream.of(id, name, description, createdTime).filter(Objects::nonNull).count() > 1)
            throw new BadRequestParameterField();

        if (id != null) return channelRepository.findByIdContaining(id, paging).getContent();
        if (name != null) return channelRepository.findByNameContaining(name, paging).getContent();
        if (description != null) return channelRepository.findByDescriptionContaining(description, paging).getContent();
        if (createdTime != null) return channelRepository.findByCreatedTimeContaining(createdTime, paging).getContent();
        return channelRepository.findAll(paging).getContent();
    }

    public Channel findById(String id) throws ChannelNotFoundException {
        return channelRepository.findById(id).orElseThrow(ChannelNotFoundException::new);
    }

    public Channel create(Channel channel) throws IdCannotBeNull {
        if (channel.getId() == null) throw new IdCannotBeNull();
        return channelRepository.save(channel);
    }

    public void update(String id, Channel updated) throws ChannelNotFoundException {
        Channel channel = channelRepository.findById(id).orElseThrow(ChannelNotFoundException::new);
        if (updated.getName() != null) channel.setName(updated.getName());
        if (updated.getDescription() != null) channel.setDescription(updated.getDescription());
        channelRepository.save(channel);
    }

    public void delete(String id) throws ChannelNotFoundException {
        if (!channelRepository.existsById(id)) throw new ChannelNotFoundException();
        channelRepository.deleteById(id);
    }
}
