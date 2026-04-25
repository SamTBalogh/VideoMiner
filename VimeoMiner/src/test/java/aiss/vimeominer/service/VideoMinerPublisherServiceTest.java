package aiss.vimeominer.service;

import aiss.vimeominer.exception.ForbiddenException;
import aiss.vimeominer.model.VideoMiner.Channel;
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

import static org.mockito.Mockito.mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoMinerPublisherServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    VideoMinerPublisherService videoMinerPublisherService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(videoMinerPublisherService, "videoMinerUrl", "http://localhost:8080/videominer/v1");
    }

    @Test
    @DisplayName("publish sends POST request to VideoMiner")
    void publish_success() throws ForbiddenException {
        Channel channel = new Channel("28359", "Tech Channel", "Tech content", "2024-01-01");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertDoesNotThrow(() -> videoMinerPublisherService.publish(channel, "Bearer test-token"));

        verify(restTemplate).exchange(
                eq("http://localhost:8080/videominer/v1/channels"),
                eq(HttpMethod.POST),
                any(),
                eq(Void.class));
    }

    @Test
    @DisplayName("publish without token throws ForbiddenException before calling VideoMiner")
    void publish_noToken_throwsForbiddenException() {
        Channel channel = new Channel("28359", "Tech Channel", null, null);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> videoMinerPublisherService.publish(channel, null)
        );
        assertTrue(exception.getMessage().contains("Authorization header"));
        verify(restTemplate, never()).exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class));
    }

    @Test
    @DisplayName("publish with empty bearer value throws ForbiddenException before calling VideoMiner")
    void publish_withEmptyBearerValue_throwsForbiddenException() {
        Channel channel = new Channel("28359", "Tech Channel", null, null);
        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> videoMinerPublisherService.publish(channel, "Bearer ")
        );
        assertTrue(exception.getMessage().contains("Authorization header"));
        verify(restTemplate, never()).exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class));
    }

    @Test
    @DisplayName("publish throws ForbiddenException when VideoMiner returns 403")
    void publish_forbidden() {
        Channel channel = new Channel("28359", "Tech Channel", null, null);

        // parseVideo expects a message containing: "message" "<value>"
        HttpClientErrorException forbidden = mock(HttpClientErrorException.class);
        when(forbidden.getMessage()).thenReturn("403 FORBIDDEN: \"{\"message\":\"Unauthorized\"}\"");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenThrow(forbidden);

        assertThrows(ForbiddenException.class, () -> videoMinerPublisherService.publish(channel, "bad-token"));
    }

    @Test
    @DisplayName("publish normalizes bearer token with tab separator")
    @SuppressWarnings("unchecked")
    void publish_withBearerTab_normalizesHeader() throws ForbiddenException {
        Channel channel = new Channel("28359", "Tech Channel", "Tech content", "2024-01-01");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok(null));

        videoMinerPublisherService.publish(channel, "Bearer\tmy-token");

        ArgumentCaptor<HttpEntity<Channel>> requestCaptor = ArgumentCaptor.forClass((Class) HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                eq(Void.class)
        );
        assertEquals("Bearer my-token", requestCaptor.getValue().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }
}
