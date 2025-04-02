package com.example.authentification.Controllers;


import com.example.authentification.Entities.User;
import com.example.authentification.Repositories.UserRepository;
import com.example.authentification.Services.JwtService;
import com.example.authentification.Services.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password) {
        System.out.println("Essai de login avec l'email : " + email);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        System.out.println("Authentification réussie pour l'utilisateur : " + email);
        return jwtService.generateToken(email);
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
