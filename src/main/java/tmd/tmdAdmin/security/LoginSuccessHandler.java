package tmd.tmdAdmin.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tmd.tmdAdmin.data.entities.User;
import tmd.tmdAdmin.data.repositories.UserRepository;
import tmd.tmdAdmin.services.UserDetailsImp;

import java.io.IOException;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException, IOException {

        UserDetailsImp userDetails = (UserDetailsImp) authentication.getPrincipal();
        User user = userDetails.getUser(); // get your actual User entity

        userRepository.updateLastLogin(user.getId(), System.currentTimeMillis());

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
