package aiss.vimeominer.service;

import aiss.vimeominer.exception.CommentsNotFoundException;
import aiss.vimeominer.model.VideoMiner.Comment;
import aiss.vimeominer.model.VimeoMiner.comment.VimeoCommentSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Value("${vimeo.token}")
    private String token;

    @Value("${vimeo.uri}")
    private String uri;

    @Autowired
    RestTemplate restTemplate;

    public List<Comment> findCommentsByVideoId(String id) throws CommentsNotFoundException {
        try {
            String url = uri + "/videos/" + id + "/comments";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<VimeoCommentSearch> request = new HttpEntity<>(null, headers);

            ResponseEntity<VimeoCommentSearch> response = restTemplate.exchange(url, HttpMethod.GET, request, VimeoCommentSearch.class);
            List<Comment> comments = response.getBody().getComments().stream().map(Comment::new).collect(Collectors.toList());
            return comments;
        }
        catch (HttpClientErrorException.NotFound e) {
            throw new CommentsNotFoundException();
        }
    }

    public List<Comment> findCommentsByVideoIdMaxComments(String id, Integer numComments) throws CommentsNotFoundException {
        try{
            String url = uri + "/videos/" + id + "/comments?per_page="+numComments;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<VimeoCommentSearch> request = new HttpEntity<>(null, headers);

            ResponseEntity<VimeoCommentSearch> response = restTemplate.exchange(url, HttpMethod.GET, request, VimeoCommentSearch.class);
            List<Comment> comments = response.getBody().getComments().stream().map(Comment::new).collect(Collectors.toList());
            return comments;
        }
        catch (HttpClientErrorException.NotFound e) {
            throw new CommentsNotFoundException();
        }
    }
}
