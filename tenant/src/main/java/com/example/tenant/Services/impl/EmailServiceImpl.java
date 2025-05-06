package com.example.tenant.Services.impl;



import com.example.tenant.Services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendCredentials(String to, String password) {
        try {
            System.out.println("Tentative d'envoi de mail à " + to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Symphonia – Identifiants de connexion");
            message.setText("Bonjour,\n\nVoici votre mot de passe temporaire : " + password +
                    "\n\nMerci de vous connecter à la plateforme Symphonia.");
            mailSender.send(message);
            System.out.println("Email envoyé avec succès.");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi d'email : " + e.getMessage());
            e.printStackTrace();
        }
    }


    /*@Override
    public void sendCredentials(String to, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Symphonia – Identifiants de connexion");
        message.setText("Bonjour,\n\nVoici votre mot de passe temporaire : " + password +
                "\n\nMerci de vous connecter à la plateforme Symphonia.");
        mailSender.send(message);
    }*/
}
