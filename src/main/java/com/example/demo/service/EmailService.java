package com.example.demo.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailRecuperacao(String emailDestino, String nomeUsuario, String linkRedefinicao) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(emailDestino);
            helper.setSubject("Redefinição de Senha - Bolão Ademicon");

            // Template HTML integrado com o Design Premium da aplicação
            String htmlContent = "<div style=\"font-family: 'Poppins', sans-serif; max-width: 600px; margin: 0 auto; padding: 30px; background: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px;\">" +
                    "<h2 style=\"color: #0f172a; font-size: 1.5rem;\">Olá, " + nomeUsuario + "!</h2>" +
                    "<p style=\"color: #475569; font-size: 1rem; line-height: 1.6;\">Recebemos uma solicitação para redefinir a senha da sua conta no Bolão Ademicon. Se não foi você, ignore este e-mail.</p>" +
                    "<div style=\"text-align: center; margin: 30px 0;\">" +
                    "  <a href=\"" + linkRedefinicao + "\" style=\"background-color: #E31B23; color: white; padding: 14px 28px; font-weight: bold; text-decoration: none; border-radius: 12px; display: inline-block; box-shadow: 0 4px 12px rgba(227,27,35,0.2);\">Redefinir Minha Senha</a>" +
                    "</div>" +
                    "<p style=\"color: #94a3b8; font-size: 0.85rem;\">Atenção: Este link é válido por apenas 15 minutos por questões de segurança.</p>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao disparar e-mail de recuperação: " + e.getMessage());
        }
    }
}