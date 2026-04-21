package aiss.YouTubeMiner.model.YouTubeModel.extended.uploads;

import com.fasterxml.jackson.annotation.*;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Uploads {

    @JsonProperty("items")
    private List<UploadItems> items;

    @JsonProperty("items")
    public List<UploadItems> getItems() {
        return items;
    }

    @JsonProperty("items")
    public void setItems(List<UploadItems> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Uploads.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
