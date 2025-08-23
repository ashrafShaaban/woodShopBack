package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "gallery_type")
@Data
public class GalleryType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    private String name;
    private String nameAr;
    private String nameRu;
    private String path;
}
