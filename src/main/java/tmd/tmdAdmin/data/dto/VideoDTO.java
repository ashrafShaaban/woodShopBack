package tmd.tmdAdmin.data.dto;

import org.springframework.beans.factory.annotation.Autowired;

public class VideoDTO {
    private int id;
    private String title;
    private String videoURL;

    public VideoDTO() {
    }

    @Autowired
    public VideoDTO(String title, String videoURL) {
        this.title = title;
        this.videoURL = videoURL;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }
}
