package aiss.videominer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

/**
 * @author Juan C. Alonso
 */
@Entity
@Table(name = "VMUser")
public class User {

    /*
    * In order to avoid making the model unnecessarily complex, we establish a one-to-one relationship between Comment and
    * User (instead of many-to-one). This causes an exception if we try to add a Comment to the DataBase that has been
    * created by a User that already has a Comment in a previously stored Video. To avoid this exception, we automatically
    * assign an id to each new User with AutoIncrement.
     */
    @Id
    @JsonProperty("id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("user_link")
    private String userLink;

    @JsonProperty("picture_link")
    private String pictureLink;

    public User() {}

    public User(String name, String userLink, String pictureLink) {
        this.name = name;
        this.userLink = userLink;
        this.pictureLink = pictureLink;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_link() {
        return userLink;
    }

    public void setUser_link(String user_link) {
        this.userLink = user_link;
    }

    public String getPicture_link() {
        return pictureLink;
    }

    public void setPicture_link(String picture_link) {
        this.pictureLink = picture_link;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", user_link='" + userLink + '\'' +
                ", picture_link='" + pictureLink + '\'' +
                '}';
    }

}
