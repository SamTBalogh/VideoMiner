package aiss.YouTubeMiner.Controller;

import aiss.YouTubeMiner.exception.*;
import aiss.YouTubeMiner.model.VideoMinerModel.Channel;
import aiss.YouTubeMiner.model.VideoMinerModel.Video;
import aiss.YouTubeMiner.model.YouTubeModel.extended.channel.ChannelUploads;
import aiss.YouTubeMiner.service.*;
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

import java.util.ArrayList;
import java.util.List;

@Tag(name="Channel", description="Channel management API")
@RestController
@RequestMapping("/youTubeMiner")
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

    @Autowired
    UploadService uploadService;

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
                                    @RequestHeader(name = "Authorization", required = false) String token) throws ChannelNotFoundException, ForbiddenException, VideoNotFoundChannelIDException, CaptionNotFoundException, CommentNotFoundException {
        try {
            RestTemplate restTemplate = new RestTemplate();

            Channel channel = channelService.findChannelById(id);
            List<Video> videos = videoService.findSearchVideosMaxChannelId(id, maxVideos);
            channel.setVideos(videos);
            for (Video video : videos) {
                video.setComments(commentService.findCommentsByVideoIdMax(video.getId(), maxComments));
                video.setCaptions(captionService.findCaptionsByVideoId(video.getId()));
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
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseYoutube(e.getMessage()));
        }


    }

    // GET http://localhost:8082/youTubeMiner/v1/{id}
    @Operation( summary = "Retrieve a Channel by Id",
            description = "Get a Channel object from the YouTube's API by specifying its id.<br /><br />" +
                    "The maximum number of videos and comments to retrieve from the channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved from the channel.",
            tags = {"channels", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema=@Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/v1/{id}")
    public Channel GetChannelVideo(@PathVariable("id") String id,
                                   @RequestParam(name = "maxVideos", defaultValue = "10") Integer maxVideos,
                                   @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments) throws ForbiddenException, ChannelNotFoundException, VideoNotFoundChannelIDException, CaptionNotFoundException, CommentNotFoundException {
        try {
                Channel channel = channelService.findChannelById(id);

                List<Video> videos = videoService.findSearchVideosMaxChannelId(id, maxVideos);

                for(Video video : videos){
                    video.setCaptions(captionService.findCaptionsByVideoId(video.getId()));
                    video.setComments(commentService.findCommentsByVideoIdMax(video.getId(), maxComments));
                }

                channel.setVideos(videos);

                return channel;
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
                                               @RequestHeader(name = "Authorization", required = false) String token) throws ForbiddenException, VideoNotFoundChannelIDException, CaptionNotFoundException, ListChannelsNotFoundException, CommentNotFoundException, ChannelNotFoundException {

        try {
            RestTemplate restTemplate = new RestTemplate();

            List<ChannelUploads> channelUploads = channelService.findSearchListChannelsByNameMax(name, maxChannels);
            List<Channel> channelList = new ArrayList<>();
            for (ChannelUploads channelUpload : channelUploads) {
                Channel channel = new Channel(channelUpload);
                List<Video> videos = videoService.findSearchVideosMaxChannelId(channel.getId(), maxVideos);
                channel.setVideos(videos);
                for (Video video : videos) {
                    video.setComments(commentService.findCommentsByVideoIdMax(video.getId(), maxComments));
                    video.setCaptions(captionService.findCaptionsByVideoId(video.getId()));
                }

                channel.setVideos(videos);
                channelList.add(channel);


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
            }

            return channelList;
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseYoutube(e.getMessage()));
        }
    }


    // GET http://localhost:8082/youTubeMiner/v1/channels
    @Operation( summary = "Retrieve a List of Channels by Id",
            description = "Get a List of Channel objects from the YouTube's API by searching by their name.<br /><br />" +
                    "The maximum number of channels to be retrieved can be specified with `maxChannels`.<br />If no values are provided, the number of channels will be 3.<br /><br />" +
                    "The maximum number of videos and comments to retrieve from each channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved for each channel.",
            tags = {"channels", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema=@Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/v1/channels")
    public List<Channel> GetListChannelsVideo(@RequestParam("name") String name,
                                              @RequestParam(name = "maxChannels", defaultValue = "3") Integer maxChannels,
                                              @RequestParam(name = "maxVideos", defaultValue = "10") Integer maxVideos,
                                              @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments) throws ForbiddenException, VideoNotFoundChannelIDException, CaptionNotFoundException, ListChannelsNotFoundException, CommentNotFoundException, ChannelNotFoundException {

        try {
            List<ChannelUploads> channelUploads = channelService.findSearchListChannelsByNameMax(name, maxChannels);
            List<Channel> channelList = new ArrayList<>();
            for (ChannelUploads channelUpload : channelUploads) {
                Channel channel = new Channel(channelUpload);
                List<Video> videos = videoService.findSearchVideosMaxChannelId(channel.getId(), maxVideos);
                channel.setVideos(videos);
                for (Video video : videos) {
                    video.setComments(commentService.findCommentsByVideoIdMax(video.getId(), maxComments));
                    video.setCaptions(captionService.findCaptionsByVideoId(video.getId()));
                }

                channel.setVideos(videos);
                channelList.add(channel);
            }
            return channelList;
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseYoutube(e.getMessage()));
        }
    }

    //
    //EMPLEANDO LOS NUEVOS MODELOS
    //



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
                                      @RequestHeader(name = "Authorization", required = false) String token) throws ChannelNotFoundException, ForbiddenException, CaptionNotFoundException, CommentNotFoundException, UploadsNotFoundException, VideoNotFoundException {


        try {
            RestTemplate restTemplate = new RestTemplate();

            ChannelUploads channelUploads = channelService.findChannelByIdContentDetails(id);
            Channel channel = new Channel(channelUploads);
            List<String> uploads = uploadService.findUploadsIdsMax(channelUploads.getUploads(), maxVideos);
            List<Video> videos = new ArrayList<>();
            for(String videoId : uploads){
                videos.add(videoService.findVideoById(videoId));
            }
            for (Video video : videos) {
                video.setComments(commentService.findCommentsByVideoIdMax(video.getId(), maxComments));
                video.setCaptions(captionService.findCaptionsByVideoId(video.getId()));
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
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseYoutube(e.getMessage()));
        }
    }

    // GET http://localhost:8082/youTubeMiner/v2/{id}
    @Operation( summary = "Retrieve a Channel by Id",
            description = "This version is using the new models implemented.<br /><br />Get a Channel object from the YouTube's API by specifying its id.<br /><br />" +
                    "The maximum number of videos and comments to retrieve from the channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved.",
            tags = {"channels", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema=@Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/v2/{id}")
    public Channel GetChannelVideoV2(@PathVariable("id") String id,
                                     @RequestParam(name = "maxVideos", defaultValue = "10") Integer maxVideos,
                                     @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments) throws ForbiddenException, ChannelNotFoundException, CaptionNotFoundException, CommentNotFoundException, UploadsNotFoundException, VideoNotFoundException {

        try {
            ChannelUploads channeUploads = channelService.findChannelByIdContentDetails(id);
            Channel channel = new Channel(channeUploads);
            List<String> uploads = uploadService.findUploadsIdsMax(channeUploads.getUploads(), maxVideos);
            List<Video> videos = new ArrayList<>();
            for(String videoId : uploads){
                videos.add(videoService.findVideoById(videoId));
            }
            channel.setVideos(videos);
            for (Video video : videos) {
                video.setComments(commentService.findCommentsByVideoIdMax(video.getId(), maxComments));
                video.setCaptions(captionService.findCaptionsByVideoId(video.getId()));
            }

            return channel;
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
                                                 @RequestHeader(name = "Authorization", required = false) String token) throws ForbiddenException, CaptionNotFoundException, ListChannelsNotFoundException, CommentNotFoundException, ChannelNotFoundException, VideoNotFoundException, UploadsNotFoundException {

        try {
            RestTemplate restTemplate = new RestTemplate();
            List<Channel> channelList = new ArrayList<>();

            List<ChannelUploads> channels = channelService.findSearchListChannelsByNameMax(name, maxChannels);

            for(ChannelUploads channelUploads : channels) {
                Channel channel = new Channel(channelUploads);
                List<String> uploads = uploadService.findUploadsIdsMax(channelUploads.getUploads(), maxVideos);
                List<Video> videos = new ArrayList<>();
                for(String videoId : uploads){
                    videos.add(videoService.findVideoById(videoId));
                }
                for (Video video : videos) {
                    video.setComments(commentService.findCommentsByVideoIdMax(video.getId(), maxComments));
                    video.setCaptions(captionService.findCaptionsByVideoId(video.getId()));
                }

                channel.setVideos(videos);
                channelList.add(channel);
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
            }

            return channelList;
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseYoutube(e.getMessage()));
        }
    }


    // GET http://localhost:8082/youTubeMiner/v2/channels
    @Operation( summary = "Retrieve a List of Channels by Id",
            description = "This version is using the new models implemented.<br /><br />Get a List of Channel objects from the YouTube's API by searching by their name.<br /><br />" +
                    "The maximum number of channels to be retrieved can be specified with `maxChannels`.<br />If no values are provided, the number of channels will be 3.<br /><br />" +
                    "The maximum number of videos and comments to retrieve from each channel can be specified with the parameters `maxVideos` and `maxComments` respectively.<br />" +
                    "If no values are provided, defaults of 10 videos and 10 comments will be retrieved for each channel.",
            tags = {"channels", "get"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema=@Schema(implementation = Channel.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema=@Schema())}),
            @ApiResponse(responseCode = "404", content = {@Content(schema=@Schema())})
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/v2/channels")
    public List<Channel> GetListChannelsVideoV2(@RequestParam("name") String name,
                                                @RequestParam(name = "maxChannels", defaultValue = "3") Integer maxChannels,
                                              @RequestParam(name = "maxVideos", defaultValue = "10") Integer maxVideos,
                                              @RequestParam(name = "maxComments", defaultValue = "10") Integer maxComments) throws ForbiddenException, CaptionNotFoundException, ListChannelsNotFoundException, CommentNotFoundException, ChannelNotFoundException, UploadsNotFoundException, VideoNotFoundException {

        try {
            List<ChannelUploads> channels = channelService.findSearchListChannelsByNameMax(name, maxChannels);
            List<Channel> channelList = new ArrayList<>();
            for(ChannelUploads channelUploads : channels) {
                Channel channel = new Channel(channelUploads);
                List<String> uploads = uploadService.findUploadsIdsMax(channelUploads.getUploads(), maxVideos);
                List<Video> videos = new ArrayList<>();
                for(String videoId : uploads){
                    videos.add(videoService.findVideoById(videoId));
                }
                for (Video video : videos) {
                    video.setComments(commentService.findCommentsByVideoIdMax(video.getId(), maxComments));
                    video.setCaptions(captionService.findCaptionsByVideoId(video.getId()));
                }

                channel.setVideos(videos);
                channelList.add(channel);
            }
            return channelList;
        } catch (HttpClientErrorException e) {
            throw new ForbiddenException(ForbiddenException.parseYoutube(e.getMessage()));
        }
    }

}
