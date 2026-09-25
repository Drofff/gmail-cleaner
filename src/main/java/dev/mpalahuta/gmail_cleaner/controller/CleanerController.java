package dev.mpalahuta.gmail_cleaner.controller;

import com.google.api.services.gmail.Gmail;
import dev.mpalahuta.gmail_cleaner.service.GmailAuthService;
import dev.mpalahuta.gmail_cleaner.service.GmailCleanerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Controller
public class CleanerController {

    private final GmailCleanerService gmailCleanerService;
    private final GmailAuthService gmailAuthService;

    public CleanerController(GmailCleanerService gmailCleanerService, GmailAuthService gmailAuthService) {
        this.gmailCleanerService = gmailCleanerService;
        this.gmailAuthService = gmailAuthService;
    }

    @GetMapping("/cleaner")
    public String index(HttpSession session) throws IOException {
        Optional<Gmail> gmail = gmailAuthService.gmailClient(session.getId());
        if (gmail.isEmpty()) {
            return "redirect:/";
        }
        return "cleaner";
    }

    @PostMapping("/cleaner")
    public String startCleaning(HttpSession session) throws IOException {
        Optional<Gmail> gmail = gmailAuthService.gmailClient(session.getId());
        if (gmail.isEmpty()) {
            return "redirect:/";
        }

        final String requestId = UUID.randomUUID().toString();
        this.gmailCleanerService.cleanAll(requestId, gmail.get());

        return "redirect:/cleaner/track/" + requestId;
    }

    @GetMapping("/cleaner/track/{requestId}")
    public ModelAndView track(@PathVariable String requestId, HttpSession session) throws IOException {
        Optional<Gmail> gmail = gmailAuthService.gmailClient(session.getId());
        if (gmail.isEmpty()) {
            return new ModelAndView("redirect:/");
        }

        final var modelAndView = new ModelAndView("cleaner-tracker");
        modelAndView.addObject("inProgress", this.gmailCleanerService.isRunning(requestId));
        return modelAndView;
    }

}
