package aiss.YouTubeMiner.model.YouTubeModel.extended.channel;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RelatedPlaylists {

    @JsonProperty("uploads")
    private String uploads;

    @JsonProperty("uploads")
    public String getUploads() {
        return uploads;
    }

    @JsonProperty("uploads")
    public void setUploads(String uploads) {
        this.uploads = uploads;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(RelatedPlaylists.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.uploads == null)?"<null>":this.uploads));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}