package tmd.tmdAdmin.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tmd.tmdAdmin.validation.PasswordMatches;

/**
 * @author : yahyai
 * @mailto : yahyai@procuredox.com
 **/
@Data
@PasswordMatches
public class ChangePasswordDto {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters long")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
