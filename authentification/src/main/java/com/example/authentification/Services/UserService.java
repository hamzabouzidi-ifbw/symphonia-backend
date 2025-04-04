package com.example.authentification.Services;


import com.example.authentification.Entities.Role;
import com.example.authentification.Entities.User;
import com.example.authentification.Repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;

@Service
public class UserService  {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email : " + email));
    }
    @PostConstruct
    public void createSuperAdmin() {
        String superAdminEmail = "raouia.ben19@gmail.com";
        String defaultPassword = "ifbw_symphonia";

        if (!userRepository.existsByEmail(superAdminEmail)) {
            // Création du Super Admin
            User superAdmin = new User(superAdminEmail, passwordEncoder.encode(defaultPassword), Role.SUPER_ADMIN);
            userRepository.save(superAdmin);
            System.out.println("✅ Super Admin créé avec succès !");

            // Envoyer un e-mail après création
            String subject = "Votre compte Super Admin est prêt !";
            String body = "<h2>Bonjour Super Admin,</h2>"
                    + "<p>Votre compte a été créé avec succès.</p>"
                    + "<p><strong>Email:</strong> " + superAdminEmail + "</p>"
                    + "<p><strong>Mot de passe:</strong> " + defaultPassword + "</p>"
                    + "<p>Veuillez changer votre mot de passe après votre première connexion.</p>";

            emailService.sendEmail(superAdminEmail, subject, body);
        } else {
            System.out.println("ℹ️ Super Admin existe déjà.");
        }
    }

    public String registerUser(String email, String password, Role role) {
        try {
            if (userRepository.existsByEmail(email)) {
                throw new UserAlreadyExistsException("L'utilisateur avec cet email existe déjà");
            }

            // Crée un nouvel utilisateur avec le mot de passe crypté
            String encodedPassword = passwordEncoder.encode(password);
            User newUser = new User(email, encodedPassword, role);
            userRepository.save(newUser);

            // Retourne une réponse
            return "Utilisateur enregistré avec succès";

        } catch (Exception e) {
            // Gérer l'exception et retourner une erreur appropriée
            throw new RuntimeException("Erreur lors de l'enregistrement de l'utilisateur: " + e.getMessage());
        }
    }
    /*public String registerUser(String email, String password, Role role) {
        try {
            // Vérifiez si l'utilisateur existe déjà
            if (userRepository.existsByEmail(email)) {
                throw new UserAlreadyExistsException("L'utilisateur avec cet email existe déjà");
            }

            // Logic pour enregistrer l'utilisateur
            User newUser = new User(email, password, role);
            userRepository.save(newUser);

            // Retourner une réponse
            return "Utilisateur enregistré avec succès";

        } catch (Exception e) {
            // Gérer l'exception et retourner une erreur appropriée
            throw new RuntimeException("Erreur lors de l'enregistrement de l'utilisateur: " + e.getMessage());
        }
    }*/

}

