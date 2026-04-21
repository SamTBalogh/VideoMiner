package aiss.YouTubeMiner.model.VideoMinerModel;

import aiss.YouTubeMiner.model.YouTubeModel.comment.YoutubeComment;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Juan C. Alonso
 */

public class Comment {

    @JsonProperty("id")
    private String id;

    @JsonProperty("text")
    private String text;

    @JsonProperty("createdOn")
    private String createdOn;

    @JsonProperty("author")
    private User author;

    public Comment() {}

    // Transformer YoutubeComment into Comment
    public Comment(YoutubeComment youtubeComment) {
        this.id = youtubeComment.getCommentSnippet().getTopLevelComment().getId();
        this.text = youtubeComment.getCommentSnippet().getTopLevelComment().getSnippet().getTextOriginal();
        this.createdOn = youtubeComment.getCommentSnippet().getTopLevelComment().getSnippet().getPublishedAt();
        this.author = new User(youtubeComment.getCommentSnippet().getTopLevelComment().getSnippet().getAuthorDisplayName(), youtubeComment.getCommentSnippet().getTopLevelComment().getSnippet().getAuthorChannelUrl(),youtubeComment.getCommentSnippet().getTopLevelComment().getSnippet().getAuthorProfileImageUrl());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", createdOn='" + createdOn + '\'' +
                ", author=" + author +
                '}';
    }
}
