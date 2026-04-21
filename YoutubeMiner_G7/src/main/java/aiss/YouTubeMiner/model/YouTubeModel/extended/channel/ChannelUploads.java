package aiss.YouTubeMiner.model.YouTubeModel.extended.channel;

import aiss.YouTubeMiner.model.VideoMinerModel.Video;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Juan C. Alonso
 */
public class ChannelUploads {

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

    @JsonProperty("uploads")
    private String uploadsId;

    public ChannelUploads() {
        this.videos = new ArrayList<>();
    }

    //Transformer YoutubeChannels into ChannelUploads
    public ChannelUploads(String id, String name, String description, String createdTime, String uploads) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdTime = createdTime;
        this.videos = new ArrayList<>();
        this.uploadsId = uploads;
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

    public String getUploads() {
        return uploadsId;
    }

    public void setUploads(String uploadsId) {
        this.uploadsId = uploadsId;
    }

    @Override
    public String toString() {
        return "ChannelUploads{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdTime='" + createdTime + '\'' +
                ", videos=" + videos +
                ", contentDetails=" + contentDetails +
                ", uploadsId=" + uploadsId +
                '}';
    }
}