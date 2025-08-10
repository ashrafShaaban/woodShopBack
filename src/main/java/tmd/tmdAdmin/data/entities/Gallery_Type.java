package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;

@Entity
@Table(name="gallery_type")
public class Gallery_Type {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private  int id;

    @Column(name = "name")
    private String name;

    @Column(name="name_ar")
    private String name_ar;

    @Column(name="name_ru")
    private String name_ru;

    @Column(name="path")
    private String path;

//    @OneToMany(mappedBy = "galleryType")
//    @JsonManagedReference
//    private List<Gallery> galleries;



    public Gallery_Type() {
    }

    public Gallery_Type(String name, String name_ar, String name_ru, String path) {
        this.name = name;
        this.name_ar = name_ar;
        this.name_ru = name_ru;
        this.path = path;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Gallery_Type{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", name_ar='" + name_ar + '\'' +
                ", name_ru='" + name_ru + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
