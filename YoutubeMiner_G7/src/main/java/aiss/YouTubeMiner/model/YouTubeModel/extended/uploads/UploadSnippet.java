package aiss.YouTubeMiner.model.YouTubeModel.extended.uploads;

import com.fasterxml.jackson.annotation.*;


@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadSnippet {

    @JsonProperty("resourceId")
    private ResourceId resourceId;

    @JsonProperty("resourceId")
    public ResourceId getResourceId() {
        return resourceId;
    }

    @JsonProperty("resourceId")
    public void setResourceId(ResourceId resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(UploadSnippet.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("resourceId");
        sb.append('=');
        sb.append(((this.resourceId == null)?"<null>":this.resourceId));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
