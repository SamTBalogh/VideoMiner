package aiss.YouTubeMiner.model.VideoMinerModel;

import aiss.YouTubeMiner.model.YouTubeModel.videoSnippet.VideoSnippet;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Juan C. Alonso
 */

public class Video {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("releaseTime")
    private String releaseTime;

    @JsonProperty("comments")
    private List<Comment> comments;

    @JsonProperty("captions")
    private List<Caption> captions;

    public Video() {}


    // Transformer for List<VideoSnippet> into Video
    public Video(VideoSnippet videoSnippet) {
        this.id = videoSnippet.getId().getVideoId();
        this.name = videoSnippet.getSnippet().getTitle();
        this.description = videoSnippet.getSnippet().getDescription();
        this.releaseTime = videoSnippet.getSnippet().getPublishedAt();
        this.comments = new ArrayList<>();
        this.captions = new ArrayList<>();
    }


    //Transformer for VideoSnippet into Video
    public Video(String id, String title, String description, String publishedAt) {
        this.id = id;
        this.name = title;
        this.description = description;
        this.releaseTime = publishedAt;
        this.comments = new ArrayList<>();
        this.captions = new ArrayList<>();
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

    public String getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(String releaseTime) {
        this.releaseTime = releaseTime;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Caption> getCaptions() {
        return captions;
    }

    public void setCaptions(List<Caption> captions) {
        this.captions = captions;
    }

    @Override
    public String toString() {
        return "Video{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", releaseTime='" + releaseTime + '\'' +
                ", comments=" + comments +
                ", captions=" + captions +
                '}';
    }
}
