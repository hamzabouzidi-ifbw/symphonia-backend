package com.example.authentification.Controllers;


import com.example.authentification.Dto.authDto;
import com.example.authentification.Entities.User;
import com.example.authentification.Repositories.UserRepository;
import com.example.authentification.Services.JwtService;
import com.example.authentification.Services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("authentification")
public class authController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserService userService;  // Ajoute cet attribut

    private final AuthenticationManager authenticationManager;

    public authController(UserRepository userRepository, JwtService jwtService, AuthenticationManager authenticationManager, UserService userService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }
    @PutMapping("/update-profile")
    @PreAuthorize("isAuthenticated()")  // Assurez-vous que l'utilisateur est authentifié
    public ResponseEntity<?> updateProfile(@RequestBody User user) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();  // L'email de l'utilisateur actuellement connecté

        // Vérifiez si l'utilisateur connecté essaie de mettre à jour son propre profil
        if (!currentEmail.equals(user.getEmail())) {
            return ResponseEntity.status(403).body("Vous ne pouvez pas mettre à jour le profil d'un autre utilisateur.");
        }

        try {
            // Mise à jour des informations utilisateur
            // Si le mot de passe est présent, on le met à jour aussi
            String newPassword = user.getPassword() != null && !user.getPassword().isEmpty() ? user.getPassword() : null;
            User updatedUser = userService.updateUserDetails(currentEmail, user.getEmail(), newPassword);
            return ResponseEntity.ok(updatedUser);  // Retourner l'utilisateur mis à jour
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody authDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Récupérer le rôle de l'utilisateur
        User user = userService.findByEmail(loginRequest.getEmail());
        String role = user.getRole().name();  // Obtient le rôle de l'utilisateur
        String jwt = jwtService.generateToken(loginRequest.getEmail(), role);  // Passe l'email et le rôle

        String message = role.equals("SUPER_ADMIN") ? "Hello Super Admin" : "Hello User";
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("role", role);
        response.put("message", message);
        response.put("userDetails", user);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/logout")
    public String logout() {
        // Logique de déconnexion, peut être simplement invalider le token côté client
        return "User logged out successfully";
    }
    @GetMapping("/user-details")
    public User getUserDetails() {
        // Récupère l'utilisateur actuellement authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        return user;
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        return userService.registerUser(user.getEmail(), user.getPassword(), user.getRole());
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/register_tenant")
    public ResponseEntity<String> register(@RequestBody authDto request) {
        userService.registerAdminTenant(request);
        return ResponseEntity.ok("Utilisateur enregistré avec succès.");
    }
    @DeleteMapping("/users/by-tenant/{tenantId}")
    public ResponseEntity<?> deleteUsersByTenant(@PathVariable Long tenantId,
                                                 @RequestHeader("role") String role) {
        if ("SUPER_ADMIN".equals(role)) {
            userService.deleteUsersByTenantId(tenantId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to delete users."));
        }
    }
}