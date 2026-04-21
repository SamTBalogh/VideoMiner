package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.ChannelNotFoundException;
import aiss.YouTubeMiner.exception.ListChannelsNotFoundException;
import aiss.YouTubeMiner.model.VideoMinerModel.Channel;
import aiss.YouTubeMiner.model.YouTubeModel.channel.ChannelSearch;
import aiss.YouTubeMiner.model.YouTubeModel.channel.ChannelSnippet;
import aiss.YouTubeMiner.model.YouTubeModel.channel.YoutubeChannel;
import aiss.YouTubeMiner.model.YouTubeModel.extended.channel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChannelServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    ChannelService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "token", "test-token");
        ReflectionTestUtils.setField(service, "uri", "https://youtube.googleapis.com/youtube/v3");
    }

    private ChannelSearch buildChannelSearch(String id, String title, String description, String published, String uploadsId) {
        ChannelSnippet snippet = new ChannelSnippet();
        snippet.setTitle(title);
        snippet.setDescription(description);
        snippet.setPublishedAt(published);

        RelatedPlaylists relatedPlaylists = new RelatedPlaylists();
        relatedPlaylists.setUploads(uploadsId);
        ChannelContentDetails contentDetails = new ChannelContentDetails();
        contentDetails.setRelatedPlaylists(relatedPlaylists);

        YoutubeChannel youtubeChannel = new YoutubeChannel();
        youtubeChannel.setId(id);
        youtubeChannel.setSnippet(snippet);
        youtubeChannel.setContent(contentDetails);

        ChannelSearch channelSearch = new ChannelSearch();
        channelSearch.setItems(List.of(youtubeChannel));
        return channelSearch;
    }

    private ChannelsSearch buildChannelsSearch(String channelId) {
        YoutubeChannelsId channelsId = new YoutubeChannelsId();
        channelsId.setChannelId(channelId);
        YoutubeChannels youtubeChannels = new YoutubeChannels();
        youtubeChannels.setId(channelsId);
        ChannelsSearch channelsSearch = new ChannelsSearch();
        channelsSearch.setItems(List.of(youtubeChannels));
        return channelsSearch;
    }

    @Test
    @DisplayName("findChannelById returns a Channel")
    void findChannelById_success() throws ChannelNotFoundException {
        ChannelSearch response = buildChannelSearch("ch1", "MrBeast", "Channel desc", "2020-01-01T00:00:00Z", "uploads1");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(ChannelSearch.class)))
                .thenReturn(ResponseEntity.ok(response));

        Channel channel = service.findChannelById("ch1");
        assertNotNull(channel);
        assertEquals("ch1", channel.getId());
        assertEquals("MrBeast", channel.getName());
    }

    @Test
    @DisplayName("findChannelById throws ChannelNotFoundException when items is null")
    void findChannelById_notFound() {
        ChannelSearch emptyResponse = new ChannelSearch();
        emptyResponse.setItems(null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(ChannelSearch.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        assertThrows(ChannelNotFoundException.class, () -> service.findChannelById("unknown"));
    }

    @Test
    @DisplayName("findChannelByIdContentDetails returns ChannelUploads")
    void findChannelByIdContentDetails_success() throws ChannelNotFoundException {
        ChannelSearch response = buildChannelSearch("ch1", "MrBeast", "desc", "2020-01-01T00:00:00Z", "UU123");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(ChannelSearch.class)))
                .thenReturn(ResponseEntity.ok(response));

        ChannelUploads channelUploads = service.findChannelByIdContentDetails("ch1");
        assertNotNull(channelUploads);
        assertEquals("ch1", channelUploads.getId());
        assertEquals("UU123", channelUploads.getUploads());
    }

    @Test
    @DisplayName("findChannelByIdContentDetails throws ChannelNotFoundException when items is null")
    void findChannelByIdContentDetails_notFound() {
        ChannelSearch emptyResponse = new ChannelSearch();
        emptyResponse.setItems(null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(ChannelSearch.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        assertThrows(ChannelNotFoundException.class, () -> service.findChannelByIdContentDetails("unknown"));
    }

    @Test
    @DisplayName("findSearchListChannelsByNameMax returns list of ChannelUploads")
    void findSearchListChannelsByNameMax_success() throws ListChannelsNotFoundException, ChannelNotFoundException {
        ChannelsSearch channelsSearch = buildChannelsSearch("ch1");
        ChannelSearch channelSearch = buildChannelSearch("ch1", "MrBeast", "desc", "2020-01-01T00:00:00Z", "UU123");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(ChannelsSearch.class)))
                .thenReturn(ResponseEntity.ok(channelsSearch));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(ChannelSearch.class)))
                .thenReturn(ResponseEntity.ok(channelSearch));

        List<ChannelUploads> result = service.findSearchListChannelsByNameMax("MrBeast", 1);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ch1", result.get(0).getId());
    }

    @Test
    @DisplayName("findSearchListChannelsByNameMax throws ListChannelsNotFoundException when list is empty")
    void findSearchListChannelsByNameMax_notFound() {
        ChannelsSearch emptyResponse = new ChannelsSearch();
        emptyResponse.setItems(List.of());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(ChannelsSearch.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        assertThrows(ListChannelsNotFoundException.class,
                () -> service.findSearchListChannelsByNameMax("nonexistent", 3));
    }
}
