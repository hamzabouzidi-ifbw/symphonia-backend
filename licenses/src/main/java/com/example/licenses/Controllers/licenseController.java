package com.example.licenses.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("licenses")
public class licenseController {

    // Méthode qui retourne "Hello World" pour tester le contrôleur
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello World";
    }
}
