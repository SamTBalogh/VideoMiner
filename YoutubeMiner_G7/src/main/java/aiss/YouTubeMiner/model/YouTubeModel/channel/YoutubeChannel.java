package aiss.YouTubeMiner.model.YouTubeModel.channel;

import aiss.YouTubeMiner.model.YouTubeModel.extended.channel.ChannelContentDetails;
import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeChannel {

    @JsonProperty("id")
    private String id;
    @JsonProperty("snippet")
    private ChannelSnippet snippet;

    //This attribute has been added manually
    @JsonProperty("contentDetails")
    private ChannelContentDetails content;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("snippet")
    public ChannelSnippet getSnippet() {
        return snippet;
    }

    @JsonProperty("snippet")
    public void setSnippet(ChannelSnippet snippet) {
        this.snippet = snippet;
    }

    @JsonProperty("contentDetails")
    public ChannelContentDetails getContent() {
        return content;
    }

    @JsonProperty("contentDetails")
    public void setContent(ChannelContentDetails content) {
        this.content = content;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(YoutubeChannel.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("snippet");
        sb.append('=');
        sb.append(((this.snippet == null)?"<null>":this.snippet));
        sb.append(',');
        sb.append("contentDetails");
        sb.append('=');
        sb.append(((this.content == null)?"<null>":this.content));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
