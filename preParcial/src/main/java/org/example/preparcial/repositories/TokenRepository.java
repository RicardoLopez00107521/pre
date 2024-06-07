package org.example.preparcial.repositories;

import org.example.preparcial.domain.entities.Token;
import org.example.preparcial.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    List<Token> findByUserAndActive(User user, Boolean active); // Buscamos los tokens activos por usuario
}
