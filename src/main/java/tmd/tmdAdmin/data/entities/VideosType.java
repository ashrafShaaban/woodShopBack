package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "video_type")
@Data
public class VideosType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String nameAr;
    private String nameRu;
    private String path;
    @OneToMany(mappedBy = "videosType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Videos> videos = new ArrayList<>();
    public void addVideo(Videos video) {
        videos.add(video);
        video.setVideosType(this);
    }

    public void removeVideo(Videos video) {
        videos.remove(video);
        video.setVideosType(null);
    }
}
