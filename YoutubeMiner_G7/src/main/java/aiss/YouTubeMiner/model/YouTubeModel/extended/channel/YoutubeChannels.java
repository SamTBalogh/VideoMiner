package aiss.YouTubeMiner.model.YouTubeModel.extended.channel;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeChannels {

    @JsonProperty("id")
    private YoutubeChannelsId id;

    @JsonProperty("id")
    public YoutubeChannelsId getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(YoutubeChannelsId id) {
        this.id = id;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(YoutubeChannels.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
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