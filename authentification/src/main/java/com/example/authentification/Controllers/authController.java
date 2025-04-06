package com.example.authentification.Controllers;


import com.example.authentification.Dto.authDto;
import com.example.authentification.Entities.User;
import com.example.authentification.Repositories.UserRepository;
import com.example.authentification.Services.JwtService;
import com.example.authentification.Services.UserService;
import org.springframework.http.ResponseEntity;
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

  /**  @PostMapping("/login")
    public String login(@RequestBody authDto loginRequest) {
        System.out.println("Essai de login avec l'email : " + loginRequest.getEmail());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        System.out.println("Authentification réussie pour l'utilisateur : " + loginRequest.getEmail());
        return jwtService.generateToken(loginRequest.getEmail());
    }*/
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody authDto loginRequest) {
      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
      );
      SecurityContextHolder.getContext().setAuthentication(authentication);

      // Générer le token JWT
      String jwt = jwtService.generateToken(loginRequest.getEmail());
      User user = userService.findByEmail(loginRequest.getEmail());
      String role = user.getRole().name();
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
    /*   @PostMapping
    @RequestMapping(value ="/login")
    public String login(@RequestParam String email, @RequestParam String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        return jwtService.generateToken(email);
    }*/
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        return userService.registerUser(user.getEmail(), user.getPassword(), user.getRole());
    }

}
