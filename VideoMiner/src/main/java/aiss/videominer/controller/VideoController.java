package aiss.videominer.controller;

import aiss.videominer.exception.*;
import aiss.videominer.model.Video;
import aiss.videominer.service.TokenService;
import aiss.videominer.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="Video", description="Video management API")
@RestController
@RequestMapping("/videoMiner/v1")
public class VideoController {

    @Autowired
    VideoService videoService;

    @Autowired
    TokenService tokenService;

    // GET http://localhost:8080/videoMiner/v1/videos
    @Operation( summary = "Retrieve a list of videos",
            description = "Get a list of videos with different options in paging, ordering and filtering. Only one of the filter parameters (`id`, `name`, `description`, `order`) may be present at the same time.<br /><br />" +
                    "Each filter parameter corresponds to one of the attributes of the Video class. For example, `id` filters videos by their unique identifier, `name` filters videos by their name, `description` filters videos by their description, and `releaseTime` filters videos by the time they were released.<br /><br />" +
                    "The parameter `page` indicates the page number of results to retrieve, while the `size` parameter specifies the number of results per page.<br />" +
                    "Pages are zero-indexed, so `page=0` returns the first page of results. If there is no result found the response will return empty.<br /><br />"+
                    "The `order` parameter specifies the ordering of the results. It accepts the name of the attribute by which you want to order the results. If descending order is desired, prefix the attribute with '-'. For example, 'name' for ascending order and '-name' for descending order.",
            tags = {"videos", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema=@Schema(implementation = Video.class)), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/videos")
    public List<Video> findAll(@RequestHeader HttpHeaders header,
                               @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String id, @RequestParam(required = false) String name,
                               @RequestParam(required = false) String description, @RequestParam(required = false) String releaseTime,
                               @RequestParam(required = false) String order) throws TokenRequiredException, TokenNotValidException, BadRequestParameterField {
        tokenService.validate(header);
        return videoService.findAll(page, size, id, name, description, releaseTime, order);
}

    // GET http://localhost:8080/videoMiner/v1/videos/{id}
    @Operation( summary = "Retrieve a Video by Id",
            description = "Get a Video object by specifying its Id.",
            tags = {"videos", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema=@Schema(implementation = Video.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/videos/{id}")
    public Video findById(@PathVariable String id, @RequestHeader HttpHeaders header) throws VideoNotFoundException, TokenRequiredException, TokenNotValidException {
        tokenService.validate(header);
        return videoService.findById(id);
    }

    // GET http://localhost:8080/videoMiner/v1/channels/{channelId}/videos
    @Operation( summary = "Retrieve the list of videos of a Channel",
            description = "Get a list of videos associated with the channel Id.",
            tags = {"videos", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(array = @ArraySchema(schema=@Schema(implementation = Video.class)), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/channels/{channelId}/videos")
    public List<Video> getAllVideosByChannel(@PathVariable("channelId") String channelId, @RequestHeader HttpHeaders header) throws ChannelNotFoundException, TokenRequiredException, TokenNotValidException {
        tokenService.validate(header);
        return videoService.findByChannel(channelId);
    }

    // POST http://localhost:8080/videoMiner/v1/channels/{channelId}/videos
    @Operation( summary = "Insert a Video into the list of videos of a Channel",
            description = "Add a Video object into the list of videos associated with the channel Id.<br >The Video data is passed in the body of the request in JSON format.",
            tags = {"videos", "post"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema=@Schema(implementation = Video.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/channels/{channelId}/videos")
    public List<Video> create(@PathVariable("channelId") String channelId, @Valid @RequestBody Video videoRequest, @RequestHeader HttpHeaders header) throws ChannelNotFoundException, TokenRequiredException, TokenNotValidException, IdCannotBeNull {
        tokenService.validate(header);
        return videoService.create(channelId, videoRequest);
    }

    // PUT http://localhost:8080/videoMiner/v1/videos/{id}
    @Operation( summary = "Update a Video",
            description = "Update a Video object by specifying its Id.<br >Nor the id, the releaseTime, the comments list or the captions list can be modified.<br >The Video data is passed in the body of the request in JSON format.",
            tags = {"videos", "put"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/videos/{id}")
    public void update(@Valid @RequestBody Video updatedVideo, @PathVariable String id, @RequestHeader HttpHeaders header) throws VideoNotFoundException, TokenRequiredException, TokenNotValidException {
        tokenService.validate(header);
        videoService.update(id, updatedVideo);
    }

    // DELETE http://localhost:8080/videoMiner/v1/videos/{id}
    @Operation( summary = "Delete a Video",
            description = "Delete a Video object by specifying its Id.<br >Because of the relation with Comment, User and Caption in the model, all the comments, users and captions linked will be deleted too.",
            tags = {"videos", "delete"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/videos/{id}")
    public void delete(@PathVariable String id, @RequestHeader HttpHeaders header) throws VideoNotFoundException, TokenRequiredException, TokenNotValidException {
        tokenService.validate(header);
        videoService.delete(id);
    }
}
