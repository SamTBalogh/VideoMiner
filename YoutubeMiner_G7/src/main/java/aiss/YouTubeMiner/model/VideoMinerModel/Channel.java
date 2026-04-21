package aiss.YouTubeMiner.model.VideoMinerModel;

import aiss.YouTubeMiner.model.YouTubeModel.extended.channel.ChannelContentDetails;
import aiss.YouTubeMiner.model.YouTubeModel.extended.channel.ChannelUploads;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Juan C. Alonso
 */
public class Channel {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("createdTime")
    private String createdTime;

    @JsonProperty("videos")
    private List<Video> videos;

    //this attribute has been added manually
    @JsonProperty("contentDetails")
    private ChannelContentDetails contentDetails;


    public Channel() {
        this.videos = new ArrayList<>();
    }

    //Transformer ChannelUploads into Channel
    public Channel(ChannelUploads channelUploads) {
        this.id = channelUploads.getId();
        this.name = channelUploads.getName();
        this.description = channelUploads.getDescription();
        this.createdTime = channelUploads.getCreatedTime();
        this.videos = new ArrayList<>();
    }

    //Transformer YoutubeChannel into Channel
    public Channel(String id, String name, String description, String createdTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdTime = createdTime;
        this.videos = new ArrayList<>();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    public ChannelContentDetails getContentDetails() {
        return contentDetails;
    }

    public void setContentDetails(ChannelContentDetails contentDetails) {
        this.contentDetails = contentDetails;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdTime='" + createdTime + '\'' +
                ", videos=" + videos +
                ", contentDetails=" + contentDetails +
                '}';
    }
}
