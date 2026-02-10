package lontar.service;

import candi.auth.core.CandiAuthService;
import candi.auth.core.CandiUser;
import lontar.model.AuthProvider;
import lontar.model.Role;
import lontar.model.User;
import lontar.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CandiAuthService authService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CandiAuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    public User getCurrentUser() {
        CandiUser candiUser = authService.getCurrentUser();
        if (candiUser instanceof User user) {
            return user;
        }
        // Fallback: look up by ID if the original entity isn't available
        if (candiUser != null) {
            Object id = candiUser.getId();
            UUID uuid = id instanceof UUID ? (UUID) id : UUID.fromString(id.toString());
            return userRepository.findById(uuid).orElse(null);
        }
        return null;
    }

    public User createOwner(String name, String email, String password) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.OWNER);
        user.setAuthProvider(AuthProvider.LOCAL);
        return userRepository.save(user);
    }

    public User createUser(String name, String email, String password, Role role, AuthProvider provider) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(name);
        user.setEmail(email);
        if (password != null) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setRole(role);
        user.setAuthProvider(provider);
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public boolean hasAnyUsers() {
        return userRepository.count() > 0;
    }

    public void updateLastLogin(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User updateProfile(User user) {
        return userRepository.save(user);
    }

    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void changeRole(UUID userId, Role role) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRole(role);
            userRepository.save(user);
        });
    }

    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }
}
