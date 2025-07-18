package com.ohse.OSHapp.controller;

import com.ohse.OSHapp.controller.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @Autowired
    private MailService mailService;

    @GetMapping("/health")
    public String health() { return "OK"; }

    @GetMapping("/test-email")
    public String testEmail(@RequestParam String to) {
        mailService.sendSimpleEmail(to, "Test Email from OHSE App", "This is a test email sent from your Spring Boot app using Gmail SMTP.");
        return "Email sent to " + to;
    }
}
