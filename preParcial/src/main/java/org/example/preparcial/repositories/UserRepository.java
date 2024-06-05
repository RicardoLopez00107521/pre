package org.example.preparcial.repositories;

import org.example.preparcial.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> { // Los repositorios facilitan la implmentacion del CRUD y consultas personalizadas en una BDD relacional
    Optional<User> findByUsernameOrEmail(String username, String email); // Optional <User> -> Indica un tipo de retorno este puede ser nulo
    Optional<User> findByActiveIsTrueAndUsernameOrEmail(String username, String email);
    Optional<User> findByUserIdAndActiveIsTrue(UUID userId);
}
