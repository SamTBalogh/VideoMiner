package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.*;
import aiss.YouTubeMiner.model.VideoMinerModel.Channel;
import aiss.YouTubeMiner.model.VideoMinerModel.Video;
import aiss.YouTubeMiner.model.YouTubeModel.extended.channel.ChannelUploads;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelAssemblerServiceTest {

    @Mock
    ChannelService channelService;

    @Mock
    VideoService videoService;

    @Mock
    CaptionService captionService;

    @Mock
    CommentService commentService;

    @Mock
    UploadService uploadService;

    @InjectMocks
    ChannelAssemblerService assemblerService;

    @Test
    @DisplayName("buildFullChannelV1 returns assembled channel with videos")
    void buildFullChannelV1_success() throws Exception {
        Channel channel = new Channel("ch1", "Test Channel", "Desc", "2024-01-01");
        Video video = new Video("v1", "Video One", "Desc", "2024-01-01");

        when(channelService.findChannelById("ch1")).thenReturn(channel);
        when(videoService.findSearchVideosMaxChannelId("ch1", 2)).thenReturn(List.of(video));
        when(commentService.findCommentsByVideoIdMax("v1", 5)).thenReturn(List.of());
        when(captionService.findCaptionsByVideoId("v1")).thenReturn(List.of());

        Channel result = assemblerService.buildFullChannelV1("ch1", 2, 5);
        assertNotNull(result);
        assertEquals("ch1", result.getId());
        assertEquals(1, result.getVideos().size());
    }

    @Test
    @DisplayName("buildFullChannelV2 returns assembled channel with videos via uploads")
    void buildFullChannelV2_success() throws Exception {
        ChannelUploads channelUploads = new ChannelUploads("ch1", "Test Channel", "Desc", "2024-01-01", "UU_abc123");
        Video video = new Video("v1", "Video One", "Desc", "2024-01-01");

        when(channelService.findChannelByIdContentDetails("ch1")).thenReturn(channelUploads);
        when(uploadService.findUploadsIdsMax("UU_abc123", 2)).thenReturn(List.of("v1"));
        when(videoService.findVideoById("v1")).thenReturn(video);
        when(commentService.findCommentsByVideoIdMax("v1", 5)).thenReturn(List.of());
        when(captionService.findCaptionsByVideoId("v1")).thenReturn(List.of());

        Channel result = assemblerService.buildFullChannelV2("ch1", 2, 5);
        assertNotNull(result);
        assertEquals("ch1", result.getId());
        assertEquals(1, result.getVideos().size());
    }

    @Test
    @DisplayName("buildFullChannelListV1 returns list of assembled channels")
    void buildFullChannelListV1_success() throws Exception {
        ChannelUploads cu = new ChannelUploads("ch1", "Test Channel", "Desc", "2024-01-01", "UU_abc123");
        Video video = new Video("v1", "Video One", "Desc", "2024-01-01");

        when(channelService.findSearchListChannelsByNameMax("test", 1)).thenReturn(List.of(cu));
        when(videoService.findSearchVideosMaxChannelId("ch1", 2)).thenReturn(List.of(video));
        when(commentService.findCommentsByVideoIdMax("v1", 5)).thenReturn(List.of());
        when(captionService.findCaptionsByVideoId("v1")).thenReturn(List.of());

        List<Channel> result = assemblerService.buildFullChannelListV1("test", 1, 2, 5);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ch1", result.get(0).getId());
    }

    @Test
    @DisplayName("buildFullChannelListV2 returns list of assembled channels via uploads")
    void buildFullChannelListV2_success() throws Exception {
        ChannelUploads cu = new ChannelUploads("ch1", "Test Channel", "Desc", "2024-01-01", "UU_abc123");
        Video video = new Video("v1", "Video One", "Desc", "2024-01-01");

        when(channelService.findSearchListChannelsByNameMax("test", 1)).thenReturn(List.of(cu));
        when(uploadService.findUploadsIdsMax("UU_abc123", 2)).thenReturn(List.of("v1"));
        when(videoService.findVideoById("v1")).thenReturn(video);
        when(commentService.findCommentsByVideoIdMax("v1", 5)).thenReturn(List.of());
        when(captionService.findCaptionsByVideoId("v1")).thenReturn(List.of());

        List<Channel> result = assemblerService.buildFullChannelListV2("test", 1, 2, 5);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ch1", result.get(0).getId());
    }
}
