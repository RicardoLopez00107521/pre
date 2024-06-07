package org.example.preparcial.services;

import org.example.preparcial.domain.dtos.*;
import org.example.preparcial.domain.entities.Assist;
import org.example.preparcial.domain.entities.Course;
import org.example.preparcial.domain.entities.Take;
import org.example.preparcial.domain.entities.User;

import java.util.List;
import java.util.UUID;

public interface CourseService {

    Course findByTittleOrUser(String tittle, String user);
    Course findByIdentifier(String identifier);
    Course findByUUID(UUID uuid);
    List<CourseResponseDTO> findAllCourses(User user);
    void createCourse(CreateCourseDTO info);

    void addAssistant(AddAssistantDTO info);
    List<Assist> findAllAssistantsByCourse(Course course, User user);
    List<UserResponseDTO> findAllAssistants(Course course);
    List<CourseResponseDTO> findAllCoursesByAssistant(User user);

    void joinCourse(InscribeDTO info, User user);
    List<Take> findAllUsersByCourse(Course course, User user);
    List<UserResponseDTO> findAllParticipants(Course course);
    List<CourseResponseDTO> findMyCoursesParticipation(User user);

    void changeCourseActiveness(UUID uuid);
    Boolean isCreator(User user, Course course);
}
