package tmd.tmdAdmin.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tmd.tmdAdmin.data.dto.ChangePasswordDto;
import tmd.tmdAdmin.data.entities.User;
import tmd.tmdAdmin.services.UserService;
import tmd.tmdAdmin.utils.ModelAttributes;

/**
 * @author : yahyai
 * @mailto : yahyai@procuredox.com
 **/
@Controller
@RequiredArgsConstructor
public class UserProfileController {
    private final UserService userService;
    private final ModelAttributes modelAttributes;

    @GetMapping("/profile")
    public String showProfilePage(Model model, HttpServletRequest request) {
        modelAttributes.setModelAttributes(model, request,"User Profile | El Dahman",null);
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        return "user-profile";
    }

    @PostMapping("/profile")
    public String changePassword(@Valid @ModelAttribute("changePasswordDto") ChangePasswordDto changePasswordDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model,
                                 HttpServletRequest request,
                                 HttpServletResponse response) { // For session invalidation
        modelAttributes.setModelAttributes(model, request,"User Profile | El Dahman",null);


        // Check for DTO validation errors
        if (bindingResult.hasErrors()) {
            return "user-profile"; // Return to the form with errors
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found!")); // Should not happen

        // 1. Verify current password
        if (!userService.checkPassword(changePasswordDto.getCurrentPassword(), currentUser.getPassword())) {
            bindingResult.rejectValue("currentPassword", "currentPassword.invalid", "Invalid current password");
            return "user-profile";
        }

        // 2. Check if new password is the same as old password
        if (userService.checkPassword(changePasswordDto.getNewPassword(), currentUser.getPassword())) {
            bindingResult.rejectValue("newPassword", "newPassword.sameAsOld", "New password cannot be the same as the old password");
            return "user-profile";
        }

        // If all checks pass, update the password
        try {
            userService.updatePassword(currentUser, changePasswordDto.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully! Please log in with your new password.");

            // Invalidate current session for security
            new SecurityContextLogoutHandler().logout(request, response, authentication);

            return "redirect:/login"; // Redirect to login page
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating password: " + e.getMessage());
            return "redirect:/profile"; // Redirect back to profile on error (without re-displaying form data)
        }
    }

}
