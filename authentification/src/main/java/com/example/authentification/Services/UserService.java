package com.example.authentification.Services;


import com.example.authentification.Entities.Role;
import com.example.authentification.Entities.User;
import com.example.authentification.Repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService  {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String registerUser(String email, String password, Role role) {
        try {
            // Vérifie si l'utilisateur existe déjà
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

