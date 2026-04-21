package aiss.videominer.service;

import aiss.videominer.exception.*;
import aiss.videominer.model.Comment;
import aiss.videominer.model.User;
import aiss.videominer.model.Video;
import aiss.videominer.repository.CommentRepository;
import aiss.videominer.repository.UserRepository;
import aiss.videominer.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    CommentRepository commentRepository;

    public List<User> findAll(int page, int size, String id, String name,
                              String userLink, String pictureLink, String order)
            throws BadRequestParameterField, BadRequestIdParameter {

        Pageable paging = PageableHelper.build(page, size, order);

        if (Stream.of(id, name, userLink, pictureLink).filter(Objects::nonNull).count() > 1)
            throw new BadRequestParameterField();

        if (id != null) {
            try {
                Long idL = Long.valueOf(id);
                return userRepository.findById(idL, paging).getContent();
            } catch (NumberFormatException e) {
                throw new BadRequestIdParameter();
            }
        }
        if (name != null) return userRepository.findByName(name, paging).getContent();
        if (userLink != null) return userRepository.findByUserLinkContaining(userLink, paging).getContent();
        if (pictureLink != null) return userRepository.findByPictureLinkContaining(pictureLink, paging).getContent();
        return userRepository.findAll(paging).getContent();
    }

    public User findById(String id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public List<User> findByVideo(String videoId) throws VideoNotFoundException {
        Video video = videoRepository.findById(videoId).orElseThrow(VideoNotFoundException::new);
        return video.getComments().stream().map(Comment::getAuthor).collect(Collectors.toList());
    }

    public void update(String id, User updated) throws UserNotFoundException {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        if (updated.getName() != null) user.setName(updated.getName());
        if (updated.getUser_link() != null) user.setUser_link(updated.getUser_link());
        if (updated.getPicture_link() != null) user.setPicture_link(updated.getPicture_link());
        userRepository.save(user);
    }

    public void delete(String id) throws UserNotFoundException {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        Comment comment = commentRepository.findByAuthor(user);
        commentRepository.delete(comment);
    }
}
