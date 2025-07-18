package com.ohse.OSHapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String resetLink, String fullName, String role) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Réinitialisation de votre mot de passe - OHSE CAPITAL");
            String greeting = String.format("Cher(e) <b>%s</b>,<br><br>", role);
            String html = "" +
                "<div style='background:linear-gradient(135deg,#fff,#f8fafc 60%,#7f1d1d 100%);padding:40px 0;font-family:sans-serif;'>" +
                "  <div style='max-width:480px;margin:0 auto;background:#fff;border-radius:18px;box-shadow:0 4px 32px #7f1d1d22;padding:32px 32px 24px 32px;'>" +
                "    <div style='text-align:center;margin-bottom:24px;'>" +
                "      <img src='https://i.ibb.co/Z1JCMDg2/Logo-ohse.png' alt='OHSE Logo' style='height:56px;margin-bottom:8px;border-radius:12px;' />" +
                "      <h2 style='color:#7f1d1d;font-size:2rem;margin:0 0 8px 0;'>OHSE CAPITAL</h2>" +
                "      <p style='color:#bfa46a;font-size:1.1rem;margin:0;'>Réinitialisation de votre mot de passe</p>" +
                "    </div>" +
                "    <p style='color:#7f1d1d;font-size:1.1rem;margin-bottom:18px;'>" + greeting +
                "      Nous avons reçu une demande de réinitialisation de votre mot de passe.<br>" +
                "      Cliquez sur le bouton ci-dessous pour définir un nouveau mot de passe :" +
                "    </p>" +
                "    <div style='text-align:center;margin-bottom:24px;'>" +
                "      <a href='" + resetLink + "' style='display:inline-block;padding:16px 36px;background:#7f1d1d;color:#fff;font-weight:bold;border-radius:8px;text-decoration:none;font-size:1.15rem;box-shadow:0 2px 8px #7f1d1d33;transition:background 0.2s;'>Réinitialiser le mot de passe</a>" +
                "      <div style='margin-top:12px;font-size:0.95rem;'><a href='" + resetLink + "' style='color:#7f1d1d;text-decoration:underline;'>Ou cliquez ici si le bouton ne fonctionne pas</a></div>" +
                "    </div>" +
                "    <p style='color:#888;font-size:0.95rem;'>" +
                "      Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email.<br><br>" +
                "      Merci,<br>L'équipe OHSE CAPITAL" +
                "    </p>" +
                "  </div>" +
                "  <div style='text-align:center;color:#bfa46a;font-size:0.9rem;margin-top:24px;'>" +
                "    © OHSE CAPITAL - Plateforme Santé & Sécurité au Travail" +
                "  </div>" +
                "</div>";
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendActivationEmail(String to, String activationLink, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Activation de votre compte - OHSE CAPITAL");
            String html = "" +
                "<div style='background:linear-gradient(135deg,#fff,#f8fafc 60%,#7f1d1d 100%);padding:40px 0;font-family:sans-serif;'>" +
                "  <div style='max-width:480px;margin:0 auto;background:#fff;border-radius:18px;box-shadow:0 4px 32px #7f1d1d22;padding:32px 32px 24px 32px;'>" +
                "    <div style='text-align:center;margin-bottom:24px;'>" +
                "      <img src='https://i.ibb.co/Z1JCMDg2/Logo-ohse.png' alt='OHSE Logo' style='height:56px;margin-bottom:8px;border-radius:12px;' />" +
                "      <h2 style='color:#7f1d1d;font-size:2rem;margin:0 0 8px 0;'>OHSE CAPITAL</h2>" +
                "      <p style='color:#bfa46a;font-size:1.1rem;margin:0;'>Activation de votre compte</p>" +
                "    </div>" +
                "    <p style='color:#7f1d1d;font-size:1.1rem;margin-bottom:18px;'>" +
                "      Bienvenue !<br><br>" +
                "      Pour activer votre compte, veuillez entrer ce code d'activation sur la page d'activation :<br>" +
                "      <span style='display:inline-block;font-size:1.2rem;font-weight:bold;color:#7f1d1d;background:#f8fafc;padding:8px 18px;border-radius:8px;margin:12px 0;'>" + code + "</span><br>" +
                "      Ce code est valable 10 minutes." +
                "    </p>" +
                (activationLink != null ?
                "    <div style='text-align:center;margin-bottom:24px;'>" +
                "      <a href='" + activationLink + "' style='display:inline-block;padding:16px 36px;background:#7f1d1d;color:#fff;font-weight:bold;border-radius:8px;text-decoration:none;font-size:1.15rem;box-shadow:0 2px 8px #7f1d1d33;transition:background 0.2s;'>Activer mon compte</a>" +
                "      <div style='margin-top:12px;font-size:0.95rem;'><a href='" + activationLink + "' style='color:#7f1d1d;text-decoration:underline;'>Ou cliquez ici si le bouton ne fonctionne pas</a></div>" +
                "    </div>" : "") +
                "    <p style='color:#888;font-size:0.95rem;'>" +
                "      Si vous n'êtes pas à l'origine de cette inscription, vous pouvez ignorer cet email.<br><br>" +
                "      Merci,<br>L'équipe OHSE CAPITAL" +
                "    </p>" +
                "  </div>" +
                "  <div style='text-align:center;color:#bfa46a;font-size:0.9rem;margin-top:24px;'>" +
                "    © OHSE CAPITAL - Plateforme Santé & Sécurité au Travail" +
                "  </div>" +
                "</div>";
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send activation email", e);
        }
    }
} 