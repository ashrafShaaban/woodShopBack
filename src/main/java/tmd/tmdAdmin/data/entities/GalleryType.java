package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
