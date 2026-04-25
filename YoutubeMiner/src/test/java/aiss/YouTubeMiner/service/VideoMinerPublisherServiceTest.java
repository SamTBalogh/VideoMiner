package aiss.YouTubeMiner.service;

import aiss.YouTubeMiner.exception.ForbiddenException;
import aiss.YouTubeMiner.model.VideoMinerModel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class VideoMinerPublisherServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    VideoMinerPublisherService publisherService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(publisherService, "videoMinerUrl", "http://localhost:8080/videoMiner/v1");
    }

    @Test
    @DisplayName("publish sends POST request successfully without exception")
    void publish_success() throws ForbiddenException {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok(null));

        Channel channel = new Channel("ch1", "Test", "Desc", "2024-01-01");
        assertDoesNotThrow(() -> publisherService.publish(channel, null));
    }

    @Test
    @DisplayName("publish sets Authorization header when token is provided")
    void publish_withToken() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok(null));

        Channel channel = new Channel("ch1", "Test", "Desc", "2024-01-01");
        assertDoesNotThrow(() -> publisherService.publish(channel, "mytoken"));
    }

    @Test
    @DisplayName("publish skips Authorization header when bearer token has no value")
    @SuppressWarnings("unchecked")
    void publish_withEmptyBearerValue_skipsAuthorizationHeader() throws ForbiddenException {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok(null));

        Channel channel = new Channel("ch1", "Test", "Desc", "2024-01-01");
        publisherService.publish(channel, "Bearer ");

        ArgumentCaptor<HttpEntity<Channel>> requestCaptor = ArgumentCaptor.forClass((Class) HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                eq(Void.class)
        );

        HttpHeaders headers = requestCaptor.getValue().getHeaders();
        assertFalse(headers.containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    @DisplayName("publish throws ForbiddenException on 403")
    void publish_forbidden() {
        // Craft message so ForbiddenException.parseVideo can extract the value
        HttpClientErrorException mockEx = mock(HttpClientErrorException.class);
        when(mockEx.getMessage()).thenReturn("403 Forbidden: {\"message\": \"Unauthorized\"}");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenThrow(mockEx);

        Channel channel = new Channel("ch1", "Test", "Desc", "2024-01-01");
        assertThrows(ForbiddenException.class, () -> publisherService.publish(channel, null));
    }

    @Test
    @DisplayName("publishAll publishes all channels without exception")
    void publishAll_success() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok(null));

        List<Channel> channels = List.of(
                new Channel("ch1", "Channel 1", "Desc", "2024-01-01"),
                new Channel("ch2", "Channel 2", "Desc", "2024-01-01")
        );
        assertDoesNotThrow(() -> publisherService.publishAll(channels, null));
    }
}
