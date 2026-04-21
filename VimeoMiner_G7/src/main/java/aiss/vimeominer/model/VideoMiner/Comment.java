package aiss.vimeominer.model.VideoMiner;

import aiss.vimeominer.model.VimeoMiner.comment.VimeoComment;
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

    public Comment(VimeoComment vimeoComment) {
        this.id = vimeoComment.getId();
        this.text = vimeoComment.getText();
        this.createdOn = vimeoComment.getCreatedOn();
        this.author = new User(vimeoComment.getUser());
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
