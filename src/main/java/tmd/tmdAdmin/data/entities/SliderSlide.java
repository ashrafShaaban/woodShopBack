package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "slider_side")
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

    public SliderSlide() {
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public String getButtonUrl() {
        return buttonUrl;
    }

    public void setButtonUrl(String buttonUrl) {
        this.buttonUrl = buttonUrl;
    }

    @Override
    public String toString() {
        return "SliderSlide{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", buttonText='" + buttonText + '\'' +
                ", buttonUrl='" + buttonUrl + '\'' +
                '}';
    }
}
