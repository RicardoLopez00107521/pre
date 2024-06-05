package org.example.preparcial.utils;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.preparcial.domain.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component // Marca una clase como componente administrador
public class JWTTools {
    @Value("Keys.secretKeyFor(SignatureAlgorithm.HS256)") // Le indicamos el formato que debe tener el String
    private String secret;

    @Value("300000") // Le indicamos el formato que debe tener el Integer
    private Integer exp;

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts
                .builder() // Lo empieza a construir
                .claims(claims) // Lo obtiene, es un Map
                .subject(user.getEmail()) // Le ponemos como subject el email del usuario
                .issuedAt(new Date(System.currentTimeMillis())) // Indicamos la fecha de emision en millis
                .expiration(new Date(System.currentTimeMillis() + exp)) // Le indicamos la fecha de expiracion
                .signWith(Keys.hmacShaKeyFor(secret.getBytes())) // Lo firmamos con el algoritmo HMAC
                .compact(); // Compacta el JWTS una cadena de texto
    }

    public Boolean verifyToken(String token) {
        try {
            JwtParser parser = Jwts // Creamos una instancia de JwtParser (analiza y valida tokens)
                    .parser() // Llamamos al metodo parser, lo que hace es descomponer el token (header, payload y firma)
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes())) // Verifica la firma del Token
                    .build(); // Finaliza la configutacion de construccion, construyendo el objeto final, el token

            parser.parse(token); // Verifica si el token es valido, ademas verifica si no ha experiado, si cumple all devuelve true sino devuelve false
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFrom(String token) {
        try {
            JwtParser parser = Jwts // Creamos una instancia de JwtParser (analiza y valida tokens)
                    .parser() // Llamamos al metodo parser
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes())) // Verifica la firma del Token
                    .build(); // Finaliza la configutacion de construccion, construyendo el objeto final

            return parser.parseSignedClaims(token) // Extrae las reclamaciones (en este caso el Username) del token valido
                    .getPayload() // Contiene los claims (reclamaciones) del token
                    .getSubject(); // Obtenemos el usuario del token asignado (En este caso debe ser el email)
        } catch (Exception e) {
            return null;
        }
    }
}
