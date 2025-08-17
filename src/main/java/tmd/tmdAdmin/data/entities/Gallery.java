package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="gallery")
public class Gallery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;



    @Column(name="imageURL")
    private String imageURL;

   @NotNull(message = "Required Failed")
    @Column(name="name",unique = true,nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name="type_Id")
    private Gallery_Type type;

    public Gallery() {
    }

    public Gallery(String imageURL, String name, Gallery_Type type) {
        this.imageURL = imageURL;
        this.name = name;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gallery_Type getType() {
        return type;
    }

    public void setType(Gallery_Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Gallery{" +
                "id=" + id +
                ", imageURL='" + imageURL + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
