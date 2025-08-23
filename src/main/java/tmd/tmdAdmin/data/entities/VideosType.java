package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.stereotype.Controller;

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
}
