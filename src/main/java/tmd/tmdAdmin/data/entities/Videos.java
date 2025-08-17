package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;

@Entity
@Table(name="videos")
public class Videos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;



    @Column(name="videoURL")
    private String videoUrl;

    @ManyToOne
    @JoinColumn(name="typeId")
    private VideosType type;

    public Videos() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public VideosType getType() {
        return type;
    }

    public void setType(VideosType type) {
        this.type = type;
    }
}
