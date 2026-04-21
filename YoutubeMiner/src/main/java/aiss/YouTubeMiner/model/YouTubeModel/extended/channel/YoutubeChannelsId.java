package aiss.YouTubeMiner.model.YouTubeModel.extended.channel;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeChannelsId {

    @JsonProperty("channelId")
    private String id;

    @JsonProperty("channelId")
    public String getChannelId() {
        return id;
    }

    @JsonProperty("channelId")
    public void setChannelId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(YoutubeChannelsId.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("channelId");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
