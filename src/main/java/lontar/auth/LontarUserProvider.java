package lontar.auth;

import candi.auth.core.CandiUser;
import candi.auth.core.CandiUserProvider;
import lontar.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LontarUserProvider implements CandiUserProvider {

    private final UserRepository userRepository;

    public LontarUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public CandiUser findByUsername(String username) {
        return userRepository.findByEmail(username).orElse(null);
    }

    @Override
    public CandiUser findById(Object id) {
        try {
            UUID uuid = id instanceof UUID ? (UUID) id : UUID.fromString(id.toString());
            return userRepository.findById(uuid).orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
