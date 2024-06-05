package org.example.preparcial.repositories;

import org.example.preparcial.domain.entities.Take;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TakesRepository extends JpaRepository<Take, UUID> {
}
