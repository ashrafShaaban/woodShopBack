package tmd.tmdAdmin.data.dto;

import jakarta.validation.constraints.NotNull;

public class GalleryItemTypeDTO {
    private  int id;
    @NotNull(message = "Required field")
    private String name;

    @NotNull(message = "Required field")
    private String name_ar;
    @NotNull(message = "Required field")
    private String name_ru;

    private String path;

    public GalleryItemTypeDTO() {
    }

    public GalleryItemTypeDTO(String name, String name_ar, String name_ru, String path) {
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
}
