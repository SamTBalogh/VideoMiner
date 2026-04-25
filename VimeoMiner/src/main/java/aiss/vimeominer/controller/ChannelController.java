package aiss.vimeominer.controller;


import aiss.vimeominer.exception.*;
import aiss.vimeominer.model.VideoMiner.Channel;
import aiss.vimeominer.service.ChannelAssemblerService;
import aiss.vimeominer.service.VideoMinerPublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@Tag(name="Channel", description="Channel management API")
@RestController
@RequestMapping("/vimeoMiner/v1")
public class ChannelController {

    @Autowired
    ChannelAssemblerService channelAssemblerService;

    @Autowired
    VideoMinerPublisherService videoMinerPublisherService;

    // POST http://localhost:8081/vimeoMiner/v1/{id}
    @Operation( summary = "Send a Channel ",
            description = "Post a Channel object to VideoMiner from the Vimeo's API by specifying the channel Id, the Channel data is sent in the body of the request in JSON format.<br /><br />" +
                    "The maximum number of videos and comments to retrieve from the channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved from each channel.<br /><br />" +
                    "Include an Authorization header with your token for authorization, taking in account that it is required for VideoMiner to authorize the request.",
            tags = {"channels", "post"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema=@Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "429", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}")
    public Channel PostChannelVideo(@PathVariable("id") String id,
        @RequestParam(name = "maxVideos", defaultValue = "10") Integer maxVideos,
        @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments,
        @RequestHeader(name = "Authorization", required = false) String token)
            throws ChannelNotFoundException, CaptionsNotFoundException, CommentsNotFoundException,
                   VideosNotFoundException, ForbiddenException, ResponseException {
        try {
            Channel channel = channelAssemblerService.buildFullChannel(id, maxVideos, maxComments);
            videoMinerPublisherService.publish(channel, token);
            return channel;
        } catch (HttpClientErrorException e) {
            throw new ResponseException(ResponseException.parseVimeo(e.getMessage()));
        }
    }

    // GET http://localhost:8081/vimeoMiner/v1/{id}
    @Operation( summary = "Retrieve a Channel by Id",
            description = "Get a Channel object from the Vimeo's API by specifying its Id.<br /><br />"+
                    "The maximum number of videos and comments to retrieve from the channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved from the channel.",
            tags = {"channels", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema=@Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "429", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public Channel GetChannelVideo(@PathVariable("id") String id,
                                    @RequestParam(name = "maxVideos", defaultValue = "10") Integer maxVideos,
                                    @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments)
            throws ChannelNotFoundException, CaptionsNotFoundException, CommentsNotFoundException,
                   VideosNotFoundException, ResponseException {
        try {
            return channelAssemblerService.buildFullChannel(id, maxVideos, maxComments);
        } catch (HttpClientErrorException e) {
            throw new ResponseException(ResponseException.parseVimeo(e.getMessage()));
        }
    }
}
