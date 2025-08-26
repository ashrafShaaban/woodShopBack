package tmd.tmdAdmin.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class UserDTO {
    private Integer id;
    private String username;
    private String password;
    private String role;
    private Boolean active = true;
}
