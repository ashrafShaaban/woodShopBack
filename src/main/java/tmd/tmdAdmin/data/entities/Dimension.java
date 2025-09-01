package tmd.tmdAdmin.data.entities;


import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "dimensions")
@Data
public class Dimension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "depth")
    private Double depth;
    @Column(name = "height")
    private Double height;
    @Column(name = "unit")
    private String unit;
    @Column(name = "width")
    private Double width;

    @ManyToMany(mappedBy = "dimensions")
    private List<Category> categories;

}
