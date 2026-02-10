package lontar.auth;

import candi.auth.core.CandiUser;
import candi.auth.social.SocialUserMapper;
import lontar.model.AuthProvider;
import lontar.model.Invite;
import lontar.model.User;
import lontar.repository.InviteRepository;
import lontar.repository.UserRepository;
import lontar.service.UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class LontarSocialUserMapper implements SocialUserMapper {

    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;
    private final UserService userService;

    public LontarSocialUserMapper(UserRepository userRepository,
                                   InviteRepository inviteRepository,
                                   UserService userService) {
        this.userRepository = userRepository;
        this.inviteRepository = inviteRepository;
        this.userService = userService;
    }

    @Override
    public CandiUser mapUser(String provider, OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String avatar = oAuth2User.getAttribute("picture");

        if (email == null) return null;

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Link Google if currently LOCAL-only
            if (user.getAuthProvider() == AuthProvider.LOCAL) {
                user.setAuthProvider(AuthProvider.BOTH);
            }
            if (avatar != null && user.getAvatar() == null) {
                user.setAvatar(avatar);
            }
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            return user;
        }

        // Check for pending invite
        Optional<Invite> pendingInvite = inviteRepository.findByEmailAndAcceptedFalse(email);
        if (pendingInvite.isPresent()) {
            Invite invite = pendingInvite.get();
            User newUser = userService.createUser(
                    name != null ? name : email,
                    email, null, invite.getRole(), AuthProvider.GOOGLE);
            newUser.setAvatar(avatar);
            newUser.setLastLoginAt(LocalDateTime.now());
            userRepository.save(newUser);

            invite.setAccepted(true);
            inviteRepository.save(invite);
            return newUser;
        }

        // No user, no invite → rejected
        return null;
    }
}
