package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "slider_side")
@Data
public class SliderSlide {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name" ,unique = true,nullable = false)
    private String name;

    @Column(name = "path")
    private String path;

    @Column(name="subtitle")
    private String subtitle;

    @Column(name="button_text")
    private String buttonText;

    @Column(name="button_url")
    private String buttonUrl;

}
