package tmd.tmdAdmin.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UserDTO {
    private Integer id;
    @NotBlank(message = "Required field")
    private String username;
    @NotBlank(message = "Required field")
    private String password;

    @NotEmpty(message = "must choose at least one role")
    private List<Integer> rolesIds = new ArrayList<>();

    public UserDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Integer> getRolesIds() {
        return rolesIds;
    }

    public void setRolesIds(List<Integer> rolesIds) {
        this.rolesIds = rolesIds;
    }
}
