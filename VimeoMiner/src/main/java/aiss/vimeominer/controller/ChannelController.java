package aiss.vimeominer.controller;


import aiss.vimeominer.exception.*;
import aiss.vimeominer.model.VideoMiner.Channel;
import aiss.vimeominer.model.VideoMiner.Video;
import aiss.vimeominer.service.CaptionService;
import aiss.vimeominer.service.ChannelService;
import aiss.vimeominer.service.CommentService;
import aiss.vimeominer.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Tag(name="Channel", description="Channel management API")
@RestController
@RequestMapping("/vimeoMiner/v1")
public class ChannelController {

    @Value("${videoMiner.url}")
    String videoMinerUrl;

    @Autowired
    ChannelService channelService;

    @Autowired
    VideoService videoService;

    @Autowired
    CaptionService captionService;

    @Autowired
    CommentService commentService;

    // POST http://localhost:8081/vimeoMiner/v1/{id}
    @Operation( summary = "Send a Channel ",
            description = "Post a Channel object to VideoMiner from the Vimeo's API by specifying the channel Id, the Channel data is sent in the body of the request in JSON format.<br /><br />" +
                    "The maximum number of videos and comments to retrieve from the channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved from each channel.<br /><br />" +
                    "Optionally, include an Authorization header with your token for authorization, taking in account that is required for VideoMiner to authorize the request.",
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
        @RequestHeader(name = "Authorization", required = false) String token) throws ChannelNotFoundException, CaptionsNotFoundException, CommentsNotFoundException, VideosNotFoundException, ForbiddenException, ResponseException {

        try {

            RestTemplate restTemplate = new RestTemplate();

        Channel channel = channelService.findChannelById(id);

        List<Video> videos = videoService.findVideosByChannelIdMaxVideos(id, maxVideos);

            for (Video video : videos) {
                video.setCaptions(captionService.findCaptionsByVideoId(video.getId()));
                video.setComments(commentService.findCommentsByVideoIdMaxComments(video.getId(), maxComments));
            }

            channel.setVideos(videos);

            HttpHeaders headers = new HttpHeaders();
            if (token != null) {
                headers.add("Authorization", token);
            }
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Channel> requestEntity = new HttpEntity<>(channel, headers);
            try {
                restTemplate.exchange(videoMinerUrl + "/channels", HttpMethod.POST, requestEntity, Void.class);
            } catch (HttpClientErrorException e) {
                throw new ForbiddenException(ForbiddenException.parseVideo(e.getMessage()));
            }
            return channel;
        } catch (HttpClientErrorException e){
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
                                    @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments) throws ChannelNotFoundException, CaptionsNotFoundException, CommentsNotFoundException, VideosNotFoundException, ResponseException {

        try {
            Channel channel = channelService.findChannelById(id);

            List<Video> videos = videoService.findVideosByChannelIdMaxVideos(id, maxVideos);

            for(Video video : videos){
                video.setCaptions(captionService.findCaptionsByVideoId(video.getId()));
                video.setComments(commentService.findCommentsByVideoIdMaxComments(video.getId(), maxComments));
            }

            channel.setVideos(videos);

            return channel;
        } catch (HttpClientErrorException e){
            throw new ResponseException(ResponseException.parseVimeo(e.getMessage()));
        }
    }
}
