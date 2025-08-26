package tmd.tmdAdmin.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tmd.tmdAdmin.data.entities.User;
import tmd.tmdAdmin.data.repositories.UserRepository;

import java.util.Optional;

/**
 * @author : yahyai
 * @mailto : yahyai@procuredox.com
 **/
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Inject BCryptPasswordEncoder

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Updates a user's password.
     * @param user The user entity to update.
     * @param newRawPassword The new password in raw (unencoded) form.
     * @return The updated User entity.
     */
    public User updatePassword(User user, String newRawPassword) {
        user.setPassword(passwordEncoder.encode(newRawPassword));
        return userRepository.save(user);
    }

    /**
     * Checks if a raw password matches the user's encoded password.
     * @param rawPassword The password to check.
     * @param encodedPassword The user's stored encoded password.
     * @return true if they match, false otherwise.
     */
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
