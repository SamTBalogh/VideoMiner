
package aiss.vimeominer.model.VimeoMiner.caption;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class VimeoCaptionSearch {

    @JsonProperty("data")
    private List<VimeoCaption> data;

    @JsonProperty("data")
    public List<VimeoCaption> getCaptions() {
        return data;
    }

    @JsonProperty("data")
    public void setCaptions(List<VimeoCaption> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(VimeoCaptionSearch.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("data");
        sb.append('=');
        sb.append(((this.data == null)?"<null>":this.data));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
