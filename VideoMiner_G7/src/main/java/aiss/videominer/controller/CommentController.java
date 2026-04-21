package aiss.videominer.controller;

import aiss.videominer.exception.*;
import aiss.videominer.model.Comment;
import aiss.videominer.model.Video;
import aiss.videominer.repository.CommentRepository;
import aiss.videominer.repository.TokenRepository;
import aiss.videominer.repository.UserRepository;
import aiss.videominer.repository.VideoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Tag(name="Comment", description="Comment management API")
@RestController
@RequestMapping("/videoMiner/v1")
public class CommentController {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenRepository tokenRepository;

    // GET http://localhost:8080/videoMiner/v1/comments
    @Operation( summary = "Retrieve a list of comments",
            description = "Get a list of comments with different options in paging, ordering and filtering. Only one of the filter parameters (`id`, `text`, `createdOn`) may be present at the same time.<br /><br />" +
                    "Each filter parameter corresponds to one of the attributes of the Comment class. For example, `id` filters comments by their unique identifier, `text` filters comments by their text and `createdOn` filters comments by the time they were created.<br /><br />" +
                    "The parameter `page` indicates the page number of results to retrieve, while the `size` parameter specifies the number of results per page.<br />" +
                    "Pages are zero-indexed, so `page=0` returns the first page of results. If there is no result found the response will return empty.<br /><br />"+
                    "The `order` parameter specifies the ordering of the results. It accepts the name of the attribute by which you want to order the results. If descending order is desired, prefix the attribute with '-'. For example, 'id' for ascending order and '-id' for descending order.",
            tags = {"comments", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema=@Schema(implementation = Comment.class)), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/comments")
    public List<Comment> findAll(@RequestHeader HttpHeaders header,
                                 @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String id, @RequestParam(required = false) String text,
                                 @RequestParam(required = false) String createdOn,@RequestParam(required = false) String order) throws TokenRequiredException, TokenNotValidException, BadRequestParameterField {
        String token = header.getFirst("Authorization");
        if (token==null) {
            throw new TokenRequiredException();
        }
        else if(tokenRepository.existsById(token)) {
            Page<Comment> pageChannels;
            Pageable paging;
            if(order!=null){
                if(order.startsWith("-")){
                    paging = PageRequest.of(page, size, Sort.by(order.substring(1)).descending());
                }
                else{
                    paging = PageRequest.of(page, size, Sort.by(order).ascending());
                }
            }else{
                paging = PageRequest.of(page, size);
            }
            int count = 0;
            if (id != null) count++;
            if (text != null) count++;
            if (createdOn != null) count++;

            if (count > 1) {
                throw new BadRequestParameterField();
            }

            if (id != null) {
                pageChannels = commentRepository.findById(id, paging);
            } else if (text != null) {
                pageChannels = commentRepository.findByTextContaining(text, paging);
            } else if (createdOn != null) {
                pageChannels = commentRepository.findByCreatedOnContaining(createdOn, paging);
            } else {
                pageChannels = commentRepository.findAll(paging);
            }
            return pageChannels.getContent();
        } else {
            throw new TokenNotValidException();
        }
    }

    // GET http://localhost:8080/videoMiner/v1/comments/{id}
    @Operation( summary = "Retrieve a Comment by Id",
            description = "Get a Comment object by specifying its Id.",
            tags = {"comments", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema=@Schema(implementation = Comment.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/comments/{id}")
    public Comment findById(@PathVariable String id, @RequestHeader HttpHeaders header) throws CommentNotFoundException, TokenRequiredException, TokenNotValidException {
        String token = header.getFirst("Authorization");
        if (token==null) {
            throw new TokenRequiredException();
        }
        else if(tokenRepository.existsById(token)) {
            Optional<Comment> comment = commentRepository.findById(id);
            if (!comment.isPresent()) {
                throw new CommentNotFoundException();
            }
            return comment.get();
        } else {
            throw new TokenNotValidException();
        }
    }

    // GET http://localhost:8080/videoMiner/v1/videos/{videoId}/comments
    @Operation( summary = "Retrieve the list of comments of a Video",
            description = "Get a list of comments associated with the video Id.",
            tags = {"comments", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema=@Schema(implementation = Comment.class)), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/videos/{videoId}/comments")
    public List<Comment> getAllCommentsByVideo(@PathVariable("videoId") String videoId, @RequestHeader HttpHeaders header) throws VideoNotFoundException, TokenRequiredException, TokenNotValidException {
        String token = header.getFirst("Authorization");
        if (token==null) {
            throw new TokenRequiredException();
        }
        else if(tokenRepository.existsById(token)) {
            Optional<Video> video = videoRepository.findById(videoId);
            if (!video.isPresent()) {
                throw new VideoNotFoundException();
            }
            return new ArrayList<>(video.get().getComments());
        } else {
            throw new TokenNotValidException();
        }
    }

    // POST http://localhost:8080/videoMiner/v1/videos/{videoId}/comments
    @Operation( summary = "Insert a Comment into the list of comments of a Video",
            description = "Add a Comment object into the list of comments associated with the video Id.<br >The Comment data is passed in the body of the request in JSON format.",
            tags = {"comments", "post"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema=@Schema(implementation = Comment.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/videos/{videoId}/comments")
    public List<Comment> create(@PathVariable("videoId") String videoId, @Valid @RequestBody Comment commentRequest, @RequestHeader HttpHeaders header) throws VideoNotFoundException, TokenRequiredException, TokenNotValidException, IdCannotBeNull {
        String token = header.getFirst("Authorization");
        if (token==null) {
            throw new TokenRequiredException();
        }
        else if(tokenRepository.existsById(token)) {
            if(commentRequest.getId() == null){
                throw new IdCannotBeNull();
            }
            Optional<Video> video = videoRepository.findById(videoId);
            if (!video.isPresent()) {
                throw new VideoNotFoundException();
            }
            video.get().getComments().add(commentRequest);
            videoRepository.save(video.get());
            return video.get().getComments();
        } else {
            throw new TokenNotValidException();
        }
    }

    // PUT http://localhost:8080/videoMiner/v1/comments/{id}
    @Operation( summary = "Update a Comment",
            description = "Update a Comment object by specifying its Id.<br >Nor the id or the author can be modified.<br >The Comment data is passed in the body of the request in JSON format.",
            tags = {"comments", "put"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/comments/{id}")
    public void update(@Valid @RequestBody Comment updatedComment, @PathVariable String id, @RequestHeader HttpHeaders header) throws CommentNotFoundException, TokenRequiredException, TokenNotValidException{
        String token = header.getFirst("Authorization");
        if (token==null) {
            throw new TokenRequiredException();
        }
        else if(tokenRepository.existsById(token)) {
            Optional<Comment> commentData = commentRepository.findById(id);
            if (!commentData.isPresent()) {
                throw new CommentNotFoundException();
            }
            Comment _comment = commentData.get();
            if(updatedComment.getText()!=null){
                _comment.setText(updatedComment.getText());
            }
            if(updatedComment.getCreatedOn()!=null){
                _comment.setCreatedOn(updatedComment.getCreatedOn());
            }
            commentRepository.save(_comment);
        } else {
            throw new TokenNotValidException();
        }
    }

    // DELETE http://localhost:8080/videoMiner/v1/comments/{id}
    @Operation( summary = "Delete a Comment",
            description = "Delete a Comment object by specifying its Id.<br >Because of the relation with User in the model, the user linked will be deleted too.",
            tags = {"comments", "delete"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/comments/{id}")
    public void delete(@PathVariable String id, @RequestHeader HttpHeaders header) throws CommentNotFoundException, TokenNotValidException, TokenRequiredException {
        String token = header.getFirst("Authorization");
        if (token==null) {
            throw new TokenRequiredException();
        }
        else if(tokenRepository.existsById(token)) {
            Optional<Comment> comment = commentRepository.findById(id);
            if(!comment.isPresent()) {
                throw new CommentNotFoundException();
            }
            commentRepository.deleteById(id);
        } else {
            throw new TokenNotValidException();
        }
    }
}
