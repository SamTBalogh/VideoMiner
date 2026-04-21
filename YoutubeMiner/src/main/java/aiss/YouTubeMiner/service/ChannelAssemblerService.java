package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.*;
import aiss.YouTubeMiner.model.VideoMinerModel.Channel;
import aiss.YouTubeMiner.model.VideoMinerModel.Video;
import aiss.YouTubeMiner.model.YouTubeModel.extended.channel.ChannelUploads;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChannelAssemblerService {

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

    public Channel buildFullChannelV1(String id, int maxVideos, int maxComments)
            throws ChannelNotFoundException, VideoNotFoundChannelIDException, CaptionNotFoundException, CommentNotFoundException {
        Channel channel = channelService.findChannelById(id);
        List<Video> videos = videoService.findSearchVideosMaxChannelId(id, maxVideos);
        for (Video video : videos) {
            video.setComments(commentService.findCommentsByVideoIdMax(video.getId(), maxComments));
            video.setCaptions(captionService.findCaptionsByVideoId(video.getId()));
        }
        channel.setVideos(videos);
        return channel;
    }

    public Channel buildFullChannelV2(String id, int maxVideos, int maxComments)
            throws ChannelNotFoundException, UploadsNotFoundException, VideoNotFoundException, CaptionNotFoundException, CommentNotFoundException {
        ChannelUploads channelUploads = channelService.findChannelByIdContentDetails(id);
        Channel channel = new Channel(channelUploads);
        List<String> uploadIds = uploadService.findUploadsIdsMax(channelUploads.getUploads(), maxVideos);
        List<Video> videos = new ArrayList<>();
        for (String videoId : uploadIds) {
            videos.add(videoService.findVideoById(videoId));
        }
        for (Video video : videos) {
            video.setComments(commentService.findCommentsByVideoIdMax(video.getId(), maxComments));
            video.setCaptions(captionService.findCaptionsByVideoId(video.getId()));
        }
        channel.setVideos(videos);
        return channel;
    }

    public List<Channel> buildFullChannelListV1(String name, int maxChannels, int maxVideos, int maxComments)
            throws ListChannelsNotFoundException, ChannelNotFoundException, VideoNotFoundChannelIDException, CaptionNotFoundException, CommentNotFoundException {
        List<ChannelUploads> channelUploads = channelService.findSearchListChannelsByNameMax(name, maxChannels);
        List<Channel> channelList = new ArrayList<>();
        for (ChannelUploads cu : channelUploads) {
            Channel channel = new Channel(cu);
            List<Video> videos = videoService.findSearchVideosMaxChannelId(cu.getId(), maxVideos);
            for (Video video : videos) {
                video.setComments(commentService.findCommentsByVideoIdMax(video.getId(), maxComments));
                video.setCaptions(captionService.findCaptionsByVideoId(video.getId()));
            }
            channel.setVideos(videos);
            channelList.add(channel);
        }
        return channelList;
    }

    public List<Channel> buildFullChannelListV2(String name, int maxChannels, int maxVideos, int maxComments)
            throws ListChannelsNotFoundException, ChannelNotFoundException, UploadsNotFoundException, VideoNotFoundException, CaptionNotFoundException, CommentNotFoundException {
        List<ChannelUploads> channelUploads = channelService.findSearchListChannelsByNameMax(name, maxChannels);
        List<Channel> channelList = new ArrayList<>();
        for (ChannelUploads cu : channelUploads) {
            Channel channel = new Channel(cu);
            List<String> uploadIds = uploadService.findUploadsIdsMax(cu.getUploads(), maxVideos);
            List<Video> videos = new ArrayList<>();
            for (String videoId : uploadIds) {
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
    }
}
