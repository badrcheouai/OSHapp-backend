package com.ohse.OSHapp.controller;

import com.ohse.OSHapp.model.Notification;
import com.ohse.OSHapp.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @GetMapping
    public List<Notification> getUserNotifications(@RequestParam String userId) {
        return notificationService.getUserNotifications(userId);
    }

    @GetMapping("/unread-count")
    public long getUnreadNotificationCount(@RequestParam String userId) {
        return notificationService.countUnreadNotifications(userId);
    }

    @PostMapping
    public Notification createNotification(@RequestBody Notification notification) {
        return notificationService.createNotification(notification);
    }

    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    @DeleteMapping("/{id}")
    public void deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
    }

    @DeleteMapping("/clear-all")
    public void clearAllNotifications(@RequestParam String userId) {
        notificationService.clearAllNotifications(userId);
    }

    @PostMapping("/simulate-rendezvous")
    public Notification simulateRendezVous(@RequestParam String email, @RequestParam String date) {
        Notification notif = new Notification();
        notif.setUserId(email); // Use email as userId for simplicity
        notif.setType("appointment_request");
        String doctorName = "Dr Badrdev"; // Default doctor name
        notif.setMessage("Un rendez-vous médical est organisé pour vous le " + date + ". " + doctorName + " vous recevra. Merci de confirmer ou de reporter si besoin.");
        notif.setLink("/dashboard/appointments/fake");
        notif.setRead(false);
        notif.setCreatedAt(java.time.LocalDateTime.now());

        Notification savedNotif = notificationService.createNotification(notif);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH'h'mm", Locale.FRENCH);
        String formattedDate = LocalDateTime.parse(date).format(formatter);

        String subject = "[IMPORTANT] Un rendez-vous médical a été organisé pour vous";

        String htmlContent = ""
            + "<div style='background:linear-gradient(135deg,#fff,#f8fafc 60%,#7f1d1d 100%);padding:40px 0;font-family:sans-serif;'>"
            + "  <div style='max-width:520px;margin:0 auto;background:#fff;border-radius:18px;box-shadow:0 4px 32px #7f1d1d22;padding:32px 32px 24px 32px;'>"
            + "    <div style='text-align:center;margin-bottom:24px;'>"
            + "      <img src='https://i.ibb.co/Z1JCMDg2/Logo-ohse.png' alt='OHSE Logo' style='height:56px;margin-bottom:8px;border-radius:12px;' />"
            + "      <h2 style='color:#7f1d1d;font-size:2rem;margin:0 0 8px 0;'>OHSE CAPITAL</h2>"
            + "      <p style='color:#bfa46a;font-size:1.1rem;margin:0;'>[IMPORTANT] Un rendez-vous médical a été organisé pour vous</p>"
            + "    </div>"
            + "    <div style='background:#fff3cd;padding:16px;border-radius:8px;margin:24px 0;font-size:1.1rem;text-align:center;'>"
            + "      <b>Rendez-vous médical :</b> " + formattedDate + "<br>"
            + "      <b>Médecin :</b> " + doctorName + ""
            + "    </div>"
            + "    <div style='text-align:center;margin-bottom:24px;'>"
            + "      <a href='http://localhost:3000/login' style='display:inline-block;padding:16px 36px;background:#7f1d1d;color:#fff;font-weight:bold;border-radius:8px;text-decoration:none;font-size:1.15rem;box-shadow:0 2px 8px #7f1d1d33;transition:background 0.2s;'>Se connecter pour confirmer ou reporter</a>"
            + "    </div>"
            + "    <p style='color:#888;font-size:0.95rem;text-align:center;'>Merci de vous connecter à l'application pour confirmer ou reporter ce rendez-vous.</p>"
            + "  </div>"
            + "  <div style='text-align:center;color:#bfa46a;font-size:0.9rem;margin-top:24px;'>© OHSE CAPITAL - Plateforme Santé & Sécurité au Travail</div>"
            + "</div>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedNotif;
    }
} 