package lontar.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lontar.repository.UserRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class SetupRedirectFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private volatile Boolean hasUsers = null;

    public SetupRedirectFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Skip static resources and setup page itself
        if (path.startsWith("/setup") || path.startsWith("/css/") || path.startsWith("/js/")
                || path.startsWith("/images/") || path.startsWith("/favicon") || path.startsWith("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Cache the check — once users exist, stop checking
        if (hasUsers == null || !hasUsers) {
            hasUsers = userRepository.count() > 0;
        }

        if (!hasUsers) {
            response.sendRedirect("/setup");
            return;
        }

        filterChain.doFilter(request, response);
    }

    public void clearCache() {
        this.hasUsers = null;
    }
}
