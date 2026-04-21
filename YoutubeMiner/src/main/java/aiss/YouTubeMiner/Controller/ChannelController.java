package aiss.YouTubeMiner.Controller;

import aiss.YouTubeMiner.exception.*;
import aiss.YouTubeMiner.model.VideoMinerModel.Channel;
import aiss.YouTubeMiner.service.ChannelAssemblerService;
import aiss.YouTubeMiner.service.VideoMinerPublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Tag(name="Channel", description="Channel management API")
@RestController
@RequestMapping("/youTubeMiner")
public class ChannelController {

    @Autowired
    ChannelAssemblerService channelAssemblerService;

    @Autowired
    VideoMinerPublisherService videoMinerPublisherService;

    // POST http://localhost:8082/youTubeMiner/v1/{id}
    @Operation(summary = "Send a Channel ",
            description = "Post a Channel object to VideoMiner from the YouTube's API by specifying the channel Id, the Channel data is sent in the body of the request in JSON format.<br /><br />" +
                    "The maximum number of videos and comments to retrieve from the channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved from each channel.<br /><br />" +
                    "Optionally, include an Authorization header with your token for authorization, taking in account that is required for VideoMiner to authorize the request.",
            tags = {"channels", "post"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema = @Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/{id}")
    public Channel PostChannelVideo(@PathVariable("id") String id,
                                    @RequestParam(name = "maxVideos", defaultValue = "10") Integer maxVideos,
                                    @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments,
                                    @RequestHeader(name = "Authorization", required = false) String token)
            throws ChannelNotFoundException, ForbiddenException, VideoNotFoundChannelIDException, CaptionNotFoundException, CommentNotFoundException {
        try {
            Channel channel = channelAssemblerService.buildFullChannelV1(id, maxVideos, maxComments);
            videoMinerPublisherService.publish(channel, token);
            return channel;
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseYoutube(e.getMessage()));
        }
    }

    // GET http://localhost:8082/youTubeMiner/v1/{id}
    @Operation(summary = "Retrieve a Channel by Id",
            description = "Get a Channel object from the YouTube's API by specifying its id.<br /><br />" +
                    "The maximum number of videos and comments to retrieve from the channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved from the channel.",
            tags = {"channels", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/v1/{id}")
    public Channel GetChannelVideo(@PathVariable("id") String id,
                                   @RequestParam(name = "maxVideos", defaultValue = "10") Integer maxVideos,
                                   @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments)
            throws ForbiddenException, ChannelNotFoundException, VideoNotFoundChannelIDException, CaptionNotFoundException, CommentNotFoundException {
        try {
            return channelAssemblerService.buildFullChannelV1(id, maxVideos, maxComments);
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseYoutube(e.getMessage()));
        }
    }

    // POST http://localhost:8082/youTubeMiner/v1/channels
    @Operation(summary = "Send a List of Channels by searching their name",
            description = "Post a series of Channel objects to VideoMiner from the YouTube's API by searching by their name, the Channel data is sent in the body of each request in JSON format.<br /><br />" +
                    "The maximum number of channels to be retrieved can be specified with `maxChannels`.<br />If no values are provided, the number of channels will be 3.<br /><br />" +
                    "The maximum number of videos and comments to retrieve from each channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved for each channel.<br /><br />" +
                    "Optionally, include an Authorization header with your token for authorization, taking in account that is required for VideoMiner to authorize the request.",
            tags = {"channels", "post"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema = @Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/channels")
    public List<Channel> PostListChannelsVideo(@RequestParam("name") String name,
                                               @RequestParam(name = "maxChannels", defaultValue = "3") Integer maxChannels,
                                               @RequestParam(name = "maxVideos", defaultValue = "10") Integer maxVideos,
                                               @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments,
                                               @RequestHeader(name = "Authorization", required = false) String token)
            throws ForbiddenException, VideoNotFoundChannelIDException, CaptionNotFoundException, ListChannelsNotFoundException, CommentNotFoundException, ChannelNotFoundException {
        try {
            List<Channel> channelList = channelAssemblerService.buildFullChannelListV1(name, maxChannels, maxVideos, maxComments);
            videoMinerPublisherService.publishAll(channelList, token);
            return channelList;
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseYoutube(e.getMessage()));
        }
    }

    // GET http://localhost:8082/youTubeMiner/v1/channels
    @Operation(summary = "Retrieve a List of Channels by Id",
            description = "Get a List of Channel objects from the YouTube's API by searching by their name.<br /><br />" +
                    "The maximum number of channels to be retrieved can be specified with `maxChannels`.<br />If no values are provided, the number of channels will be 3.<br /><br />" +
                    "The maximum number of videos and comments to retrieve from each channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved for each channel.",
            tags = {"channels", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/v1/channels")
    public List<Channel> GetListChannelsVideo(@RequestParam("name") String name,
                                              @RequestParam(name = "maxChannels", defaultValue = "3") Integer maxChannels,
                                              @RequestParam(name = "maxVideos", defaultValue = "10") Integer maxVideos,
                                              @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments)
            throws ForbiddenException, VideoNotFoundChannelIDException, CaptionNotFoundException, ListChannelsNotFoundException, CommentNotFoundException, ChannelNotFoundException {
        try {
            return channelAssemblerService.buildFullChannelListV1(name, maxChannels, maxVideos, maxComments);
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseYoutube(e.getMessage()));
        }
    }

    // POST http://localhost:8082/youTubeMiner/v2/{id}
    @Operation(summary = "Send a Channel ",
            description = "This version is using the new models implemented.<br /><br />Post a Channel object to VideoMiner from the YouTube's API by specifying the channel Id, the Channel data is sent in the body of the request in JSON format.<br /><br />" +
                    "The maximum number of videos and comments to retrieve from the channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved.<br /><br />" +
                    "Optionally, include an Authorization header with your token for authorization, taking in account that is required for VideoMiner to authorize the request.",
            tags = {"channels", "post"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema = @Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v2/{id}")
    public Channel PostChannelVideoV2(@PathVariable("id") String id,
                                      @RequestParam(name = "maxVideos", defaultValue = "10") Integer maxVideos,
                                      @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments,
                                      @RequestHeader(name = "Authorization", required = false) String token)
            throws ChannelNotFoundException, ForbiddenException, CaptionNotFoundException, CommentNotFoundException, UploadsNotFoundException, VideoNotFoundException {
        try {
            Channel channel = channelAssemblerService.buildFullChannelV2(id, maxVideos, maxComments);
            videoMinerPublisherService.publish(channel, token);
            return channel;
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseYoutube(e.getMessage()));
        }
    }

    // GET http://localhost:8082/youTubeMiner/v2/{id}
    @Operation(summary = "Retrieve a Channel by Id",
            description = "This version is using the new models implemented.<br /><br />Get a Channel object from the YouTube's API by specifying its id.<br /><br />" +
                    "The maximum number of videos and comments to retrieve from the channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved.",
            tags = {"channels", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/v2/{id}")
    public Channel GetChannelVideoV2(@PathVariable("id") String id,
                                     @RequestParam(name = "maxVideos", defaultValue = "10") Integer maxVideos,
                                     @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments)
            throws ForbiddenException, ChannelNotFoundException, CaptionNotFoundException, CommentNotFoundException, UploadsNotFoundException, VideoNotFoundException {
        try {
            return channelAssemblerService.buildFullChannelV2(id, maxVideos, maxComments);
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseYoutube(e.getMessage()));
        }
    }

    // POST http://localhost:8082/youTubeMiner/v2/channels
    @Operation(summary = "Send a series of Channels by searching their name",
            description = "This version is using the new models implemented.<br /><br />Post a series of Channel objects to VideoMiner from the YouTube's API by searching by their name, the Channel data is sent in the body of each requests in JSON format.<br /><br />" +
                    "The maximum number of channels to be retrieved can be specified with `maxChannels`.<br />If no values are provided, the number of channels will be 3.<br /><br />" +
                    "The maximum number of videos and comments to retrieve from each channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved for each channel.<br /><br />" +
                    "Optionally, include an Authorization header with your token for authorization, taking in account that is required for VideoMiner to authorize the request.",
            tags = {"channels", "post"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {@Content(schema = @Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v2/channels")
    public List<Channel> PostListChannelsVideoV2(@RequestParam("name") String name,
                                                 @RequestParam(name = "maxChannels", defaultValue = "3") Integer maxChannels,
                                                 @RequestParam(name = "maxVideos", defaultValue = "10") Integer maxVideos,
                                                 @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments,
                                                 @RequestHeader(name = "Authorization", required = false) String token)
            throws ForbiddenException, CaptionNotFoundException, ListChannelsNotFoundException, CommentNotFoundException, ChannelNotFoundException, VideoNotFoundException, UploadsNotFoundException {
        try {
            List<Channel> channelList = channelAssemblerService.buildFullChannelListV2(name, maxChannels, maxVideos, maxComments);
            videoMinerPublisherService.publishAll(channelList, token);
            return channelList;
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseYoutube(e.getMessage()));
        }
    }

    // GET http://localhost:8082/youTubeMiner/v2/channels
    @Operation(summary = "Retrieve a List of Channels by Id",
            description = "This version is using the new models implemented.<br /><br />Get a List of Channel objects from the YouTube's API by searching by their name.<br /><br />" +
                    "The maximum number of channels to be retrieved can be specified with `maxChannels`.<br />If no values are provided, the number of channels will be 3.<br /><br />" +
                    "The maximum number of videos and comments to retrieve from each channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved for each channel.",
            tags = {"channels", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/v2/channels")
    public List<Channel> GetListChannelsVideoV2(@RequestParam("name") String name,
                                                @RequestParam(name = "maxChannels", defaultValue = "3") Integer maxChannels,
                                                @RequestParam(name = "maxVideos", defaultValue = "10") Integer maxVideos,
                                                @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments)
            throws ForbiddenException, CaptionNotFoundException, ListChannelsNotFoundException, CommentNotFoundException, ChannelNotFoundException, UploadsNotFoundException, VideoNotFoundException {
        try {
            return channelAssemblerService.buildFullChannelListV2(name, maxChannels, maxVideos, maxComments);
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseYoutube(e.getMessage()));
        }
    }
}
