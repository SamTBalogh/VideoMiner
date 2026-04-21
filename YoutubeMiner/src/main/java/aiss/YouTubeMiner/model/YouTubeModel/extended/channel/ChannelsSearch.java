package aiss.YouTubeMiner.model.YouTubeModel.extended.channel;

import java.util.List;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelsSearch {

    @JsonProperty("items")
    private List<YoutubeChannels> items;

    @JsonProperty("items")
    public List<YoutubeChannels> getItems() {
        return items;
    }

    @JsonProperty("items")
    public void setItems(List<YoutubeChannels> items) {
        this.items = items;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ChannelsSearch.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("items");
        sb.append('=');
        sb.append(((this.items == null)?"<null>":this.items));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
