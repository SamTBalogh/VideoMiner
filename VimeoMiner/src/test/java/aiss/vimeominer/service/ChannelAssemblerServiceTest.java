package aiss.vimeominer.service;

import aiss.vimeominer.exception.*;
import aiss.vimeominer.model.VideoMiner.Caption;
import aiss.vimeominer.model.VideoMiner.Channel;
import aiss.vimeominer.model.VideoMiner.Comment;
import aiss.vimeominer.model.VideoMiner.Video;
import aiss.vimeominer.model.VimeoMiner.video.VimeoVideo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    ChannelAssemblerService channelAssemblerService;

    private Video buildVideo(String id) {
        VimeoVideo v = new VimeoVideo();
        v.setId("/videos/" + id);
        v.setName("Video " + id);
        return new Video(v);
    }

    @Test
    @DisplayName("buildFullChannel assembles channel with videos, captions and comments")
    void buildFullChannel_success() throws Exception {
        Channel mockChannel = new Channel("28359", "Tech Channel", "Tech content", "2024-01-01");
        Video mockVideo = buildVideo("919411397");
        List<Caption> mockCaptions = Collections.emptyList();
        List<Comment> mockComments = Collections.emptyList();

        when(channelService.findChannelById("28359")).thenReturn(mockChannel);
        when(videoService.findVideosByChannelIdMaxVideos("28359", 5)).thenReturn(List.of(mockVideo));
        when(captionService.findCaptionsByVideoId("919411397")).thenReturn(mockCaptions);
        when(commentService.findCommentsByVideoIdMaxComments("919411397", 3)).thenReturn(mockComments);

        Channel result = channelAssemblerService.buildFullChannel("28359", 5, 3);

        assertNotNull(result);
        assertEquals("28359", result.getId());
        assertEquals("Tech Channel", result.getName());
        assertEquals(1, result.getVideos().size());
        assertEquals("919411397", result.getVideos().get(0).getId());

        verify(captionService).findCaptionsByVideoId("919411397");
        verify(commentService).findCommentsByVideoIdMaxComments("919411397", 3);
    }

    @Test
    @DisplayName("buildFullChannel propagates ChannelNotFoundException")
    void buildFullChannel_channelNotFound() throws Exception {
        when(channelService.findChannelById("bad-id")).thenThrow(new ChannelNotFoundException());

        assertThrows(ChannelNotFoundException.class,
                () -> channelAssemblerService.buildFullChannel("bad-id", 10, 10));
    }

    @Test
    @DisplayName("buildFullChannel propagates VideosNotFoundException")
    void buildFullChannel_videosNotFound() throws Exception {
        Channel mockChannel = new Channel("28359", "Tech Channel", null, null);
        when(channelService.findChannelById("28359")).thenReturn(mockChannel);
        when(videoService.findVideosByChannelIdMaxVideos("28359", 10)).thenThrow(new VideosNotFoundException());

        assertThrows(VideosNotFoundException.class,
                () -> channelAssemblerService.buildFullChannel("28359", 10, 10));
    }

    @Test
    @DisplayName("buildFullChannel with no videos sets empty video list")
    void buildFullChannel_noVideos() throws Exception {
        Channel mockChannel = new Channel("28359", "Tech Channel", null, null);
        when(channelService.findChannelById("28359")).thenReturn(mockChannel);
        when(videoService.findVideosByChannelIdMaxVideos("28359", 10)).thenReturn(Collections.emptyList());

        Channel result = channelAssemblerService.buildFullChannel("28359", 10, 10);

        assertNotNull(result);
        assertTrue(result.getVideos().isEmpty());
        verifyNoInteractions(captionService, commentService);
    }
}
