package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Album Name (English) cannot be empty.")
    @Size(max = 255, message = "Album Name (English) must be less than 255 characters.")
    private String name;
    @NotBlank(message = "Album Name (Arabic) cannot be empty.")
    @Size(max = 255, message = "Album Name (Arabic) must be less than 255 characters.")
    private String nameAr;
    @NotBlank(message = "Album Name (Russian) cannot be empty.")
    @Size(max = 255, message = "Album Name (Russian) must be less than 255 characters.")
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
