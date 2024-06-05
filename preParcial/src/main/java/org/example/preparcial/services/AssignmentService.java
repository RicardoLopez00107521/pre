package org.example.preparcial.services;

import org.example.preparcial.domain.dtos.*;
import org.example.preparcial.domain.entities.Assignment;
import org.example.preparcial.domain.entities.Course;
import org.example.preparcial.domain.entities.Submit;
import org.example.preparcial.domain.entities.User;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface AssignmentService {

    Assignment findByTittle(String tittle);
    Assignment findByUUID(UUID uuid);
    List<AssignmentResponseDTO> findAllAssignments(Course course);

    void createAssignment(CreateAssignmentDTO info, Course course, Date limitDate);
    Boolean isActive(Assignment assignment);

    Date validDate(String limitDate);
    Boolean notBefore(Date limitDate);

    void makeSubmit(MakeSubmitDTO info, Assignment assignment, User user);

    List<Submit> findSubmitsToGrade (String assignmentTittle, String user, String course);
    void gradeAssignment(List<Submit> submits, GradeAssignmentDTO info);

    List<SubmitResponseDTO> findByGradedIsFalseAndUser(User user);
    List<SubmitResponseDTO> findByGradedIsTrueAndUser(User user);

    List<ToSubmitResponseDTO> findByGradedIsFalseAndCourse(Course course);
    List<SubmitResponseDTO> findAllSubmitsGraded(Course course);
}
