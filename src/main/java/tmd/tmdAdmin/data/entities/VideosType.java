package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;

@Entity
@Table(name = "videostype")
public class VideosType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",unique = true,nullable = false)
    private  int id;

    @NotNull(message = "required field")
    @Column(name="name")
    private String name;

    @NotNull(message = "required field")
    @Column(name="name_ar")
    private String name_ar;

    @NotNull(message = "required field")
    @Column(name="name_ru")
    private String name_ru;

    @Column(name = "videoURL")
    private String videoURL;

    public VideosType() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName_ar() {
        return name_ar;
    }

    public void setName_ar(String name_ar) {
        this.name_ar = name_ar;
    }

    public String getName_ru() {
        return name_ru;
    }

    public void setName_ru(String name_ru) {
        this.name_ru = name_ru;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }
}
