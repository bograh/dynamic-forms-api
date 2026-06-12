package dev.ograh.dynamicforms.web.controller;

import dev.ograh.dynamicforms.auth.dto.AuthResponse;
import dev.ograh.dynamicforms.auth.dto.LoginRequest;
import dev.ograh.dynamicforms.auth.dto.RegisterRequest;
import dev.ograh.dynamicforms.web.client.ApiException;
import dev.ograh.dynamicforms.web.client.WebApiClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebAuthController {

    private final WebApiClient apiClient;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String logout,
                            @RequestParam(required = false) String expired,
                            Model model) {
        if (logout != null) model.addAttribute("logoutMessage", "You have been logged out.");
        if (expired != null) model.addAttribute("error", "Your session has expired. Please log in again.");
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            AuthResponse response = apiClient.post(
                    "/api/auth/login",
                    new LoginRequest(email, password),
                    AuthResponse.class,
                    session
            );
            applySession(session, response);
            return "redirect:/forms";
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           HttpSession session,
                           Model model) {
        try {
            AuthResponse response = apiClient.post(
                    "/api/auth/register",
                    new RegisterRequest(name, email, password),
                    AuthResponse.class,
                    session
            );
            applySession(session, response);
            return "redirect:/forms";
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "auth/register";
        }
    }

    private void applySession(HttpSession session, AuthResponse response) {
        session.setAttribute("accessToken", response.token());
        session.setAttribute("role", response.user().role());
        session.setAttribute("userName", response.user().name());
        session.setAttribute("email", response.user().email());

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                response.user().email(),
                null,
                List.of(new SimpleGrantedAuthority(response.user().role()))
        );
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, ctx);
    }
}
