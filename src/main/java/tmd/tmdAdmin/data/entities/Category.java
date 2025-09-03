package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="categories")
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;


    @NotBlank(message = "Album Name (English) cannot be empty.")
    @Size(max = 255, message = "Album Name (English) must be less than 255 characters.")
    @Column(name = "name")
    private String name;
    @NotBlank(message = "Album Name (Arabic) cannot be empty.")
    @Size(max = 255, message = "Album Name (Arabic) must be less than 255 characters.")
    @Column(name = "nameAr")
    private String nameAr;
    @NotBlank(message = "Album Name (Russian) cannot be empty.")
    @Size(max = 255, message = "Album Name (Russian) must be less than 255 characters.")
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
    private List<Products> products =new ArrayList<>() ;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
            name = "categories_dimensions",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns =@JoinColumn(name = "dimensions_id")
    )
    private List<Dimension> dimensions;

    public void addProduct(Products product) {
        products.add(product);
        product.setCategory(this);
    }

    public void removeProduct(Products product) {
        products.remove(product);
        product.setCategory(null);
    }

}
