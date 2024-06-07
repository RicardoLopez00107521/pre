package org.example.preparcial.repositories;

import org.example.preparcial.domain.entities.Assist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssistantRepository extends JpaRepository<Assist, UUID> {
}
