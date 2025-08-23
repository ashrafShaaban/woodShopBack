package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Entity
@Table(name="categories")
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;
    @Column(name = "nameAr")
    private String nameAr;
    @Column(name = "nameRu")
    private String nameRu;
    @Column(name = "description")
    private String description;
    @Column(name = "descriptionAr")
    private String descriptionAr;
    @Column(name = "descriptionRu")
    private String descriptionRu;
    @Column(name = "material")
    private String material;
    @Column(name = "createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private Set<Products> products;

}
