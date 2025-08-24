package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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
    @OneToMany(mappedBy = "galleryType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Gallery> images = new ArrayList<>();


    public void addImage(Gallery image) {
        images.add(image);
        image.setGalleryType(this);
    }

    public void removeImage(Gallery image) {
        images.remove(image);
        image.setGalleryType(null);
    }

}
