package aiss.videominer.controller;


import aiss.videominer.exception.*;
import aiss.videominer.model.Channel;
import aiss.videominer.repository.CaptionRepository;
import aiss.videominer.repository.ChannelRepository;
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

import java.util.List;
import java.util.Optional;

@Tag(name="Channel", description="Channel management API")
@RestController
@RequestMapping("/videoMiner/v1")
public class ChannelController {

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    CaptionRepository captionRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenRepository tokenRepository;

    // GET http://localhost:8080/videoMiner/v1/channels
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/channels")
    @Operation( summary = "Retrieve a list of channels",
            description = "Get a list of channels with different options in paging, ordering and filtering. Only one of the filter parameters (`id`, `name`, `description`, `createdTime`) may be present at the same time.<br /><br />" +
                    "Each filter parameter corresponds to one of the attributes of the Channel class. For example, `id` filters channels by their unique identifier, `name` filters channels by their name, `description` filters channels by their description, and `createdTime` filters channels by the time they were created.<br /><br />" +
                    "The parameter `page` indicates the page number of results to retrieve, while the `size` parameter specifies the number of results per page.<br />" +
                    "Pages are zero-indexed, so `page=0` returns the first page of results. If there is no result found the response will return empty.<br /><br />"+
                    "The `order` parameter specifies the ordering of the results. It accepts the name of the attribute by which you want to order the results. If descending order is desired, prefix the attribute with '-'. For example, 'name' for ascending order and '-name' for descending order.",
            tags = {"channels", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema=@Schema(implementation = Channel.class)), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())})
    })
    public List<Channel> findAll(@RequestHeader HttpHeaders header,
                                 @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String id, @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String description, @RequestParam(required = false) String createdTime,
                                 @RequestParam(required = false) String order) throws TokenNotValidException, TokenRequiredException, BadRequestParameterField {
        String token = header.getFirst("Authorization");
        if (token==null) {
            throw new TokenRequiredException();
        }
        else if(tokenRepository.existsById(token)) {
            Page<Channel> pageChannels;
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
            if (name != null) count++;
            if (description != null) count++;
            if (createdTime != null) count++;

            if (count > 1) {
                throw new BadRequestParameterField();
            }

            if (id != null) {
                pageChannels = channelRepository.findByIdContaining(id, paging);
            } else if (name != null) {
                pageChannels = channelRepository.findByNameContaining(name, paging);
            } else if (description != null) {
                pageChannels = channelRepository.findByDescriptionContaining(description, paging);
            } else if (createdTime != null) {
                pageChannels = channelRepository.findByCreatedTimeContaining(createdTime, paging);
            } else {
                pageChannels = channelRepository.findAll(paging);
            }
            return pageChannels.getContent();
        } else {
            throw new TokenNotValidException();
        }
    }

    // GET http://localhost:8080/videoMiner/v1/channels/{id}
    @Operation( summary = "Retrieve a Channel by Id",
            description = "Get a Channel object by specifying its Id.",
            tags = {"channels", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema=@Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/channels/{id}")
    public Channel findById(@PathVariable String id, @RequestHeader HttpHeaders header) throws ChannelNotFoundException, TokenRequiredException, TokenNotValidException {
        String token = header.getFirst("Authorization");
        if (token==null) {
            throw new TokenRequiredException();
        }
        else if(tokenRepository.existsById(token)) {
            Optional<Channel> channel = channelRepository.findById(id);
            if (!channel.isPresent()) {
                throw new ChannelNotFoundException();
            }
            return channel.get();
        } else {
            throw new TokenNotValidException();
        }
    }

    // POST http://localhost:8080/videoMiner/v1/channels
    @Operation( summary = "Insert a Channel",
            description = "Add a Channel object, the Channel data is passed in the body of the request in JSON format.",
            tags = {"channels", "post"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema=@Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/channels")
    public Channel create(@Valid @RequestBody Channel channel, @RequestHeader HttpHeaders header) throws TokenNotValidException, TokenRequiredException, IdCannotBeNull {
        String token = header.getFirst("Authorization");
        if (token==null) {
            throw new TokenRequiredException();
        }
        else if(tokenRepository.existsById(token)) {
            if(channel.getId() == null){
                throw new IdCannotBeNull();
            }

            return channelRepository.save(channel);
        } else {
            throw new TokenNotValidException();
        }
    }

    // PUT http://localhost:8080/videoMiner/v1/channels/{id}
    @Operation( summary = "Update a Channel",
            description = "Update a Channel object by specifying its Id.<br >Nor the id, the createdTime or the videos list can be modified.<br >The Channel data is passed in the body of the request in JSON format.",
            tags = {"channels", "put"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/channels/{id}")
    public void update(@Valid @RequestBody Channel updatedChannel, @PathVariable String id, @RequestHeader HttpHeaders header) throws ChannelNotFoundException, TokenRequiredException, TokenNotValidException {
        String token = header.getFirst("Authorization");
        if (token==null) {
            throw new TokenRequiredException();
        }
        else if(tokenRepository.existsById(token)) {
            Optional<Channel> channelData = channelRepository.findById(id);
            if (!channelData.isPresent()) {
                throw new ChannelNotFoundException();
            }
            Channel _channel = channelData.get();
            if (updatedChannel.getName() != null) {
                _channel.setName(updatedChannel.getName());
            }
            if (updatedChannel.getDescription() != null) {
                _channel.setDescription(updatedChannel.getDescription());
            }
            channelRepository.save(_channel);
        } else {
            throw new TokenNotValidException();
        }
    }

    // DELETE http://localhost:8080/videoMiner/v1/channels/{id}
    @Operation( summary = "Delete a Channel",
            description = "Delete a Channel object by specifying its Id.<br >Because of the relation with Video, Comment, User and Caption in the model, all the videos, comments, users and captions linked will be deleted too.",
            tags = {"channels", "delete"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/channels/{id}")
    public void delete(@PathVariable String id, @RequestHeader HttpHeaders header) throws TokenRequiredException, TokenNotValidException, ChannelNotFoundException {
        String token = header.getFirst("Authorization");
        if (token==null) {
            throw new TokenRequiredException();
        }
        else if(tokenRepository.existsById(token)) {
            if(channelRepository.existsById(id)) {
                channelRepository.deleteById(id);
            }
            else{
                throw new ChannelNotFoundException();
            }
        } else {
            throw new TokenNotValidException();
        }
    }
}
