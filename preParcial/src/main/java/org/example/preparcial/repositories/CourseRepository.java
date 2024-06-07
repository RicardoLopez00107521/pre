package org.example.preparcial.repositories;

import org.example.preparcial.domain.entities.Course;
import org.example.preparcial.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    Optional<Course> findByActiveIsTrueAndTittleOrUser(String tittle, User user);
    Optional<Course> findByCourseIdAndActiveIsTrue(UUID courseId);
}
