package dev.mpalahuta.gmail_cleaner.controller;

import com.google.api.services.gmail.Gmail;
import dev.mpalahuta.gmail_cleaner.service.GmailAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Controller
public class LoginController {

    private static final String CALLBACK_PATH = "/oauth2/callback";
    private static final String STATE_ATTRIBUTE = "oauth2State";

    private final GmailAuthService gmailAuthService;

    public LoginController(GmailAuthService gmailAuthService) {
        this.gmailAuthService = gmailAuthService;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) throws IOException {
        Optional<Gmail> gmail = gmailAuthService.gmailClient(session.getId());
        if (gmail.isPresent()) {
            model.addAttribute("email", gmail.get().users().getProfile("me").execute().getEmailAddress());
        }
        return "index";
    }

    @GetMapping("/login")
    public String login(HttpSession session, HttpServletRequest request) {
        String state = UUID.randomUUID().toString();
        session.setAttribute(STATE_ATTRIBUTE, state);
        return "redirect:" + gmailAuthService.buildAuthorizationUrl(redirectUri(request), state);
    }

    @GetMapping(CALLBACK_PATH)
    public String callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpSession session,
            HttpServletRequest request,
            Model model
    ) throws IOException {
        Object expectedState = session.getAttribute(STATE_ATTRIBUTE);
        session.removeAttribute(STATE_ATTRIBUTE);

        if (error != null) {
            model.addAttribute("error", "Authorization was denied: " + error);
            return "error";
        }
        if (expectedState == null || !expectedState.equals(state)) {
            model.addAttribute("error", "Invalid OAuth2 state.");
            return "error";
        }
        if (code == null) {
            model.addAttribute("error", "Missing authorization code.");
            return "error";
        }

        gmailAuthService.exchangeCode(session.getId(), code, redirectUri(request));
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) throws IOException {
        gmailAuthService.revoke(session.getId());
        session.invalidate();
        return "redirect:/";
    }

    private String redirectUri(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromContextPath(request).path(CALLBACK_PATH).toUriString();
    }
}
