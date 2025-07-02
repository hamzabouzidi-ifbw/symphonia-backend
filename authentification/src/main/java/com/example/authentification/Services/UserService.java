package com.example.authentification.Services;


import com.example.authentification.Entities.Role;
import com.example.authentification.Dto.authDto;
import com.example.authentification.Entities.User;
import com.example.authentification.Repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.List;


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

    public void registerAdminTenant(authDto request) {
        // Créer l'utilisateur
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        // Convertir le String 'role' en Role enum
        Role role = Role.valueOf(request.getRole().toUpperCase()); // Assure-toi que le rôle est en majuscule

        user.setRole(role);  // Assigner l'énumération Role à l'utilisateur
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setTenantId(request.getTenantId());
        System.out.print(request.getTenantId());
        // Sauvegarder dans la base de données
        userRepository.save(user);
    }
    @Transactional
    public void deactivateUsersByTenant(Long tenantId) {
        List<User> users = userRepository.findUsersByTenantId(tenantId);
        users.forEach(user -> user.setActive(false));
        userRepository.saveAll(users);
    }
    @PostConstruct
    public void createSuperAdmin() {
        String superAdminEmail = "raniabensalem53@gmail.com";
        String defaultPassword = "rania123";

        if (!userRepository.existsByEmail(superAdminEmail)) {
            // Création du Super Admin
            User superAdmin = new User(superAdminEmail, passwordEncoder.encode(defaultPassword), Role.SUPER_ADMIN);
            userRepository.save(superAdmin);
            System.out.println("Super Admin créé avec succès !");

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

    public User updateUserDetails(String currentEmail, String newEmail, String newPassword) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (newEmail != null && !newEmail.isEmpty() && !newEmail.equals(currentEmail)) {
            user.setEmail(newEmail);  // Mise à jour de l'email
        }
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));  // Mise à jour du mot de passe
        }

        return userRepository.save(user);  // Enregistrer l'utilisateur mis à jour
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
    public void registerSipUser(authDto request) {
        // NE PAS refaire la vérification ici si déjà faite côté tenant
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        user.setTenantId(request.getTenantId());

        userRepository.save(user);
    }


    public void deleteUsersByTenantId(Long tenantId) {
        try {
            User users = userRepository.findByTenantId(tenantId);

            userRepository.delete(users);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression des utilisateurs du tenant: " + e.getMessage());
        }
    }
}