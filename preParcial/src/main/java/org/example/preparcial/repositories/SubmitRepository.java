package org.example.preparcial.repositories;

import org.example.preparcial.domain.entities.Assignment;
import org.example.preparcial.domain.entities.Course;
import org.example.preparcial.domain.entities.Submit;
import org.example.preparcial.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmitRepository extends JpaRepository<Submit, UUID> {
    Optional<Submit> findByUserAndAssignmentCourseAndAssignment(User user, Course course, Assignment assignment);
    Optional<List<Submit>> findByGradedIsFalseAndUser(User user); // Obteber mis tareas no calificadas
    Optional<List<Submit>> findByGradedIsTrueAndUser(User user); // Obteber mis tareas calificadas
}
