package com.example.authentification.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // Clé secrète sécurisée de 256 bits (32 octets) pour HS256
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Durée d'expiration du token (10 heures ici)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    // Génère un token JWT pour un utilisateur
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)  // Ajoute l'email comme sujet du token
                .claim("role", role)  // Ajoute le rôle dans le token
                .setIssuedAt(new Date())  // Définir la date de création
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))  // Définir la date d'expiration
                .signWith(SECRET_KEY)  // Signature du token avec la clé secrète
                .compact();
    }

    // Extraire l'email (le sujet) du token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);  // Récupère le "subject" (email)
    }

    // Extraire le rôle du token
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));  // Récupère le rôle
    }

    // Extraire une réclamation spécifique (par exemple, l'email ou d'autres infos)
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);  // Récupère toutes les réclamations du token
        return claimsResolver.apply(claims);  // Applique la fonction pour extraire la donnée désirée
    }

    private Claims extractAllClaims(String token) {
        try {
            // Utiliser la méthode `parseClaimsJws` avec JwtParserBuilder dans jjwt 0.11.x+
            return Jwts.parserBuilder()  // Utiliser `parserBuilder()` dans la version 0.11.x+
                    .setSigningKey(SECRET_KEY)  // Utilise la clé secrète pour vérifier la signature
                    .build()  // Crée un `JwtParser` à partir du builder
                    .parseClaimsJws(token)  // Parse le token et extrait les réclamations
                    .getBody();  // Retourne le corps des réclamations
        } catch (Exception e) {
            throw new IllegalArgumentException("Token invalide ou expiré", e);
        }
    }


    // Vérifie si le token est expiré
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());  // Compare la date d'expiration avec la date actuelle
    }

    // Extraire la date d'expiration du token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);  // Récupère la date d'expiration
    }

    // Vérifie si le token est valide
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token) && extractUsername(token) != null;  // Si le token n'est pas expiré et a un sujet
        } catch (IllegalArgumentException e) {
            return false;  // Le token est invalide ou corrompu
        }
    }
}