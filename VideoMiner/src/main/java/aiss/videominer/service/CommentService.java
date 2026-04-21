package aiss.videominer.service;

import aiss.videominer.exception.BadRequestParameterField;
import aiss.videominer.exception.CommentNotFoundException;
import aiss.videominer.exception.IdCannotBeNull;
import aiss.videominer.exception.VideoNotFoundException;
import aiss.videominer.model.Comment;
import aiss.videominer.model.Video;
import aiss.videominer.repository.CommentRepository;
import aiss.videominer.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class CommentService {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    VideoRepository videoRepository;

    public List<Comment> findAll(int page, int size, String id, String text,
                                 String createdOn, String order)
            throws BadRequestParameterField {

        Pageable paging = PageableHelper.build(page, size, order);

        if (Stream.of(id, text, createdOn).filter(Objects::nonNull).count() > 1)
            throw new BadRequestParameterField();

        if (id != null) return commentRepository.findById(id, paging).getContent();
        if (text != null) return commentRepository.findByTextContaining(text, paging).getContent();
        if (createdOn != null) return commentRepository.findByCreatedOnContaining(createdOn, paging).getContent();
        return commentRepository.findAll(paging).getContent();
    }

    public Comment findById(String id) throws CommentNotFoundException {
        return commentRepository.findById(id).orElseThrow(CommentNotFoundException::new);
    }

    public List<Comment> findByVideo(String videoId) throws VideoNotFoundException {
        Video video = videoRepository.findById(videoId).orElseThrow(VideoNotFoundException::new);
        return new ArrayList<>(video.getComments());
    }

    public List<Comment> create(String videoId, Comment comment) throws VideoNotFoundException, IdCannotBeNull {
        if (comment.getId() == null) throw new IdCannotBeNull();
        Video video = videoRepository.findById(videoId).orElseThrow(VideoNotFoundException::new);
        video.getComments().add(comment);
        videoRepository.save(video);
        return video.getComments();
    }

    public void update(String id, Comment updated) throws CommentNotFoundException {
        Comment comment = commentRepository.findById(id).orElseThrow(CommentNotFoundException::new);
        if (updated.getText() != null) comment.setText(updated.getText());
        if (updated.getCreatedOn() != null) comment.setCreatedOn(updated.getCreatedOn());
        commentRepository.save(comment);
    }

    public void delete(String id) throws CommentNotFoundException {
        commentRepository.findById(id).orElseThrow(CommentNotFoundException::new);
        commentRepository.deleteById(id);
    }
}
