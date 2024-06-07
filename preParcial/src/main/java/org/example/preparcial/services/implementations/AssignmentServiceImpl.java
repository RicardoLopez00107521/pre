package org.example.preparcial.services.implementations;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.preparcial.domain.dtos.*;
import org.example.preparcial.domain.entities.Assignment;
import org.example.preparcial.domain.entities.Course;
import org.example.preparcial.domain.entities.Submit;
import org.example.preparcial.domain.entities.User;
import org.example.preparcial.repositories.AssignmentRepository;
import org.example.preparcial.repositories.SubmitRepository;
import org.example.preparcial.services.AssignmentService;
import org.example.preparcial.services.UserService;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AssignmentServiceImpl implements AssignmentService {

    private final SubmitRepository submitRepository;

    private final UserService userService;

    private final AssignmentRepository assignmentRepository;

    public AssignmentServiceImpl(SubmitRepository submitRepository, UserService userService, AssignmentRepository assignmentRepository) {
        this.submitRepository = submitRepository;
        this.userService = userService;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public Assignment findByTittle(String tittle) {

        return assignmentRepository
                .findByTittle(tittle)
                .orElse(null);
    }

    @Override
    public Assignment findByUUID(UUID uuid) {

        return assignmentRepository
                .findById(uuid)
                .orElse(null);
    }

    @Override
    public List<AssignmentResponseDTO> findAllAssignments(Course course) {
        return assignmentRepository
                .findAll()
                .stream()
                .filter(assignment -> assignment.getCourse().getTittle().equals(course.getTittle()))
                .map(assignment -> new AssignmentResponseDTO(assignment.getCreationDate(), assignment.getTittle(), assignment.getDescription(),
                                                            assignment.getMultipleSubmits(), assignment.getLimitDate()))
                .collect(Collectors.toList());
    }

    //**************************************************** CRUD ****************************************************
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void createAssignment(CreateAssignmentDTO info, Course course, Date limitDate) {

        Assignment assignment = new Assignment();

        assignment.setTittle(info.getAssignmentName());
        assignment.setDescription(info.getAssignmentDescription());
        assignment.setMultipleSubmits(info.getMultipleSubmits());
        assignment.setLimitDate(limitDate);
        assignment.setCourse(course);
        assignment.setCreationDate(Date.from(Instant.now()));

        assignmentRepository.save(assignment);
    }

    @Override
    public Boolean isActive(Assignment assignment) {
        return !assignment.getLimitDate().before(Date.from(Instant.now()));
    }

    @Override
    public Date validDate(String limitDate) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(limitDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean notBefore(Date limitDate) {

        return !Date.from(Instant.now()).after(limitDate);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void makeSubmit(MakeSubmitDTO info, Assignment assignment, User user) {
        Submit submit = new Submit();

        submit.setAssignment(assignment);
        submit.setUser(user);
        submit.setSubmitDate(Date.from(Instant.now()));
        submit.setGraded(false);
        submit.setDescription(info.getDescription());

        submitRepository.save(submit);
    }

    @Override
    public List<Submit> findSubmitsToGrade (String assignmentTittle, String user, String course) {

        return submitRepository
                .findAll()
                .stream()
                .filter(submit -> submit.getUser().getEmail().equals(user))
                .filter(submit -> submit.getAssignment().getCourse().getTittle().equals(course))
                .filter(submit -> submit.getAssignment().getTittle().equals(assignmentTittle))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void gradeAssignment(List<Submit> submits, GradeAssignmentDTO info) {

        String instructor = userService.findUserAuthenticated().getEmail();

        if (info.getNote() > 10) {
            info.setNote(10.0);
        }

        if (info.getNote() < 0) {
            info.setNote(0.0);
        }

        submits.forEach(submit -> {
            submit.setNote(info.getNote());
            submit.setGradeObservations(info.getNoteObservations());
            submit.setGraded(true);
            submit.setGradeDate(Date.from(Instant.now()));
            submit.setInstructor(instructor);
            submitRepository.save(submit);
        });

    }

    @Override
    public List<SubmitResponseDTO> findByGradedIsFalseAndUser(User user) {

        return submitRepository
                .findAll()
                .stream()
                .filter(submit -> submit.getGraded().equals(false))
                .filter(submit -> submit.getUser().getEmail().equals(user.getEmail()))
                .map(submit -> new SubmitResponseDTO(submit.getSubmitDate(), null, null, null, null, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<SubmitResponseDTO> findByGradedIsTrueAndUser(User user) {
        return submitRepository
                .findAll()
                .stream()
                .filter(submit -> submit.getGraded().equals(true))
                .filter(submit -> submit.getUser().getEmail().equals(user.getEmail()))
                .map(submit -> new SubmitResponseDTO(submit.getSubmitDate(), submit.getNote(), submit.getGradeObservations(),
                        submit.getGradeDate(), submit.getInstructor(), submit.getAssignment().getTittle()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ToSubmitResponseDTO> findByGradedIsFalseAndCourse(Course course) {
        return submitRepository
                .findAll()
                .stream()
                .filter(submit -> submit.getGraded().equals(false))
                .filter(submit -> submit.getAssignment().getCourse().getTittle().equals(course.getTittle()))
                .map(submit -> new ToSubmitResponseDTO(submit.getSubmitDate(), submit.getUser().getEmail(), submit.getAssignment().getCourse().getTittle(),
                                                        submit.getAssignment().getTittle()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SubmitResponseDTO> findAllSubmitsGraded(Course course) {
        return submitRepository
                .findAll()
                .stream()
                .filter(submit -> submit.getAssignment().getCourse().getTittle().equals(course.getTittle()))
                .filter(submit -> submit.getGraded().equals(true))
                .map(submit -> new SubmitResponseDTO(submit.getSubmitDate(), submit.getNote(), submit.getGradeObservations(),
                                                    submit.getGradeDate(), submit.getInstructor(),submit.getAssignment().getTittle()))
                .collect(Collectors.toList());
    }
    //**************************************************** CRUD ****************************************************
}
