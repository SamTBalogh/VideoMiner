package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.CommentNotFoundException;
import aiss.YouTubeMiner.model.VideoMinerModel.Comment;
import aiss.YouTubeMiner.model.YouTubeModel.comment.CommentSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    RestTemplate restTemplate;

    @Value("${youtube.api.token}")
    private String token;

    @Value("${youtube.api.uri}")
    private String uri;

    public List<Comment> findCommentsByVideoId(String id) throws CommentNotFoundException {
        try {
            String url = uri+"/commentThreads?part=snippet&videoId=" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-goog-api-key",token);
            HttpEntity<CommentSearch> request = new HttpEntity<>(null, headers);
            try{
                ResponseEntity<CommentSearch> response = restTemplate.exchange(url, HttpMethod.GET,request, CommentSearch.class);
                return response.getBody().getItems().stream().map(Comment::new).collect(Collectors.toList());
            }
            catch (HttpClientErrorException.Forbidden e){
                return new ArrayList<>();
            }
        }
        catch (HttpClientErrorException.NotFound e) {
            throw new CommentNotFoundException();
        }
    }

    public List<Comment> findCommentsByVideoIdMax(String id, Integer num) throws CommentNotFoundException {
        try {
            String url = uri+"/commentThreads?part=snippet&maxResults="+num+"&videoId="+id;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-goog-api-key", token);
            HttpEntity<CommentSearch> request = new HttpEntity<>(null,headers);
            try{
                ResponseEntity<CommentSearch> response = restTemplate.exchange(url, HttpMethod.GET,request, CommentSearch.class);
                return response.getBody().getItems().stream().map(Comment::new).collect(Collectors.toList());
            }
            catch (HttpClientErrorException.Forbidden e){
                return new ArrayList<>();
            }
        }
        catch (HttpClientErrorException.NotFound e) {
            throw new CommentNotFoundException();
        }
    }
}

