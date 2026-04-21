package aiss.vimeominer.service;

import aiss.vimeominer.exception.ChannelNotFoundException;
import aiss.vimeominer.model.VimeoMiner.channel.VimeoChannel;
import aiss.vimeominer.model.VideoMiner.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ChannelService {

    @Value("${vimeo.token}")
    private String token;

    @Value("${vimeo.uri}")
    private String uri;

    @Autowired
    RestTemplate restTemplate;

    public Channel findChannelById(String id) throws ChannelNotFoundException {
        try {
            String url = uri + "/channels/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<VimeoChannel> request = new HttpEntity<>(null, headers);

            ResponseEntity<VimeoChannel> response = restTemplate.exchange(url, HttpMethod.GET, request, VimeoChannel.class);

            VimeoChannel vimeoChannel = response.getBody();
            Channel channel = new Channel(id, vimeoChannel.getName(), vimeoChannel.getDescription(), vimeoChannel.getCreatedTime());
            return channel;
        }
        catch (HttpClientErrorException.NotFound e) {
            throw new ChannelNotFoundException();
        }
    }

}
