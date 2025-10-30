package com.cpd.mono_video_encoder.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailSimples(String para, String assunto, String texto) {
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom("email@gmail.com");
            mensagem.setTo(para);
            mensagem.setSubject(assunto);
            mensagem.setText(texto);

            mailSender.send(mensagem);

            log.info("E-mail enviado com sucesso para {}", para);

        } catch (Exception e) {
            log.error("Erro ao tentar enviar e-mail: {}", e.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail", e);
        }
    }
}