package aiss.vimeominer.service;

import aiss.vimeominer.exception.*;
import aiss.vimeominer.model.VideoMiner.Channel;
import aiss.vimeominer.model.VideoMiner.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelAssemblerService {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private CaptionService captionService;

    @Autowired
    private CommentService commentService;

    public Channel buildFullChannel(String id, int maxVideos, int maxComments)
            throws ChannelNotFoundException, VideosNotFoundException, CaptionsNotFoundException, CommentsNotFoundException {
        Channel channel = channelService.findChannelById(id);
        List<Video> videos = videoService.findVideosByChannelIdMaxVideos(id, maxVideos);
        for (Video video : videos) {
            video.setCaptions(captionService.findCaptionsByVideoId(video.getId()));
            video.setComments(commentService.findCommentsByVideoIdMaxComments(video.getId(), maxComments));
        }
        channel.setVideos(videos);
        return channel;
    }
}
