package org.example.preparcial.domain.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.preparcial.domain.entities.Token;

@Data
@NoArgsConstructor
public class TokenDTO {

    private String token; // Esto es lo que recibira el Token

    public TokenDTO(Token token) {
        this.token = token.getContent(); // Asigna el token para el usuario
    }
}
