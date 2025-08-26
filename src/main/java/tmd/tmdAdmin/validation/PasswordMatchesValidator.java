package tmd.tmdAdmin.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import tmd.tmdAdmin.data.dto.ChangePasswordDto;

/**
 * @author : yahyai
 * @mailto : yahyai@procuredox.com
 **/
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        // No special initialization needed
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj instanceof ChangePasswordDto user) {
            // Ensure both passwords are not null before comparing
            return user.getNewPassword() != null && user.getNewPassword().equals(user.getConfirmPassword());
        }
        return false; // Not a ChangePasswordDto object
    }
}
