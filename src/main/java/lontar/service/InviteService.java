package lontar.service;

import lontar.model.Invite;
import lontar.model.Role;
import lontar.model.User;
import lontar.repository.InviteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InviteService {

    private final InviteRepository inviteRepository;

    public InviteService(InviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }

    public Invite create(String email, Role role, User invitedBy) {
        // Revoke any existing pending invite for this email
        inviteRepository.findByEmailAndAcceptedFalse(email).ifPresent(existing -> {
            inviteRepository.delete(existing);
        });

        Invite invite = new Invite();
        invite.setEmail(email);
        invite.setRole(role);
        invite.setToken(UUID.randomUUID().toString());
        invite.setInvitedBy(invitedBy);
        invite.setExpiresAt(LocalDateTime.now().plusDays(7));
        return inviteRepository.save(invite);
    }

    public List<Invite> findPending() {
        return inviteRepository.findAll().stream()
                .filter(i -> !i.isAccepted())
                .filter(i -> i.getExpiresAt() == null || i.getExpiresAt().isAfter(LocalDateTime.now()))
                .toList();
    }

    public Optional<Invite> findByToken(String token) {
        return inviteRepository.findByToken(token);
    }

    public void revoke(UUID id) {
        inviteRepository.deleteById(id);
    }
}
