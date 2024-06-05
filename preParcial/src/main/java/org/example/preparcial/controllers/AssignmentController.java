package org.example.preparcial.controllers;

import org.example.preparcial.domain.dtos.*;
import org.example.preparcial.domain.entities.*;
import org.example.preparcial.services.AssignmentService;
import org.example.preparcial.services.CourseService;
import org.example.preparcial.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/")
public class AssignmentController {

    private final AssignmentService assignmentService;

    private final CourseService courseService;

    private final UserService userService;

    public AssignmentController(AssignmentService assignmentService, CourseService courseService, UserService userService) {
        this.assignmentService = assignmentService;
        this.courseService = courseService;
        this.userService = userService;
    }

    @PostMapping("/{courseTittle}/addAssignment")
    public ResponseEntity<GeneralResponse> addAssignment(@PathVariable String courseTittle, @RequestBody CreateAssignmentDTO info) {

        Course course = courseService.findByIdentifier(courseTittle);

        if (course == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "Course not found");
        }

        User user = userService.findUserAuthenticated();
        Boolean isOwner = courseService.isCreator(user, course);

        if (!isOwner) {
            return GeneralResponse.getResponse(HttpStatus.FORBIDDEN, "Only yhe owner can add assignment");
        }

        Date limitDate = assignmentService.validDate(info.getAssignmentDate());

        if (limitDate == null) {
            return GeneralResponse.getResponse(HttpStatus.BAD_REQUEST, "Assignment limit date invalid");
        }

        Boolean validDate = assignmentService.notBefore(limitDate);

        if (!validDate) {
            return GeneralResponse.getResponse(HttpStatus.BAD_REQUEST, "Assignment date is before current date");
        }

        Assignment assignment = assignmentService.findByTittle(info.getAssignmentName());

        if (assignment != null) {
            return GeneralResponse.getResponse(HttpStatus.CONFLICT, "Assignment already exists");
        }

        assignmentService.createAssignment(info, course, limitDate);

        return GeneralResponse.getResponse(HttpStatus.CREATED, "Assignment created successfully");
    }

    @PostMapping("{course}/{assignment}/submitAssignment")
    public ResponseEntity<GeneralResponse> addSubmit (@PathVariable String course, @PathVariable String assignment, @RequestBody MakeSubmitDTO info) {

        Course courseFound = courseService.findByIdentifier(course);
        Assignment assignmentFound = assignmentService.findByTittle(assignment);

        if (courseFound == null || assignmentFound == null) {
            return GeneralResponse.getResponse(HttpStatus.CONFLICT, "Assignment not exists");
        }

        User userFound = userService.findUserAuthenticated();
        List<Take> participants = courseService.findAllUsersByCourse(courseFound, userFound);

        if (participants.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.BAD_REQUEST, "You dont participate in this course");
        }

        Boolean active = assignmentService.isActive(assignmentFound);

        if (!active) {
            return GeneralResponse.getResponse(HttpStatus.CONFLICT, "Assignment submit is not available");
        }

        Boolean multipleSubmits = assignmentFound.getMultipleSubmits();
        List<Submit> assignSubmits = assignmentService.findSubmitsToGrade(assignment, userFound.getEmail(), course);

        if (!multipleSubmits && !assignSubmits.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.CONFLICT, "You already have a previous submit");
        }

        assignmentService.makeSubmit(info, assignmentFound, userFound);

        return GeneralResponse.getResponse(HttpStatus.CREATED, "Submit successfully");
    }

    @PostMapping("{course}/{user}/{assignment}/gradeAssignment")
    public ResponseEntity<GeneralResponse> gradeAssignment(@PathVariable String course, @PathVariable String user, @PathVariable String assignment, @RequestBody GradeAssignmentDTO info) {

        Course courseFound = courseService.findByIdentifier(course);
        User userFound = userService.findUserByIdentifier(user);
        Assignment assignmentFound = assignmentService.findByTittle(assignment);

        if (courseFound == null || userFound == null || assignmentFound == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "Not found");
        }

        User owner = userService.findUserAuthenticated();
        Boolean isOwner = courseService.isCreator(owner, courseFound);

        List<Assist> isAssistant = courseService.findAllAssistantsByCourse(courseFound, owner);

        if (!isOwner && isAssistant.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.FORBIDDEN, "Only the owner or the instructor can add grade assignment");
        }

        List<Submit> assignSubmits = assignmentService.findSubmitsToGrade(assignment, user, course);

        if (assignSubmits.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "Not submits found");
        }

        assignmentService.gradeAssignment(assignSubmits, info);

        return GeneralResponse.getResponse(HttpStatus.CREATED, "Assignment graded successfully");
    }

    @GetMapping("{course}/pendientAssigment")
    public ResponseEntity<GeneralResponse> getPendient(@PathVariable String course) {

        Course courseFound = courseService.findByIdentifier(course);

        if (courseFound == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "Course not found");
        }

        User userFound = userService.findUserAuthenticated();
        List<Take> participants = courseService.findAllUsersByCourse(courseFound, userFound);

        if (participants.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.CONFLICT, "You dont participate in this course");
        }

        List<SubmitResponseDTO> pendient = assignmentService.findByGradedIsFalseAndUser(userFound);

        if (pendient.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "All your assignments have graded");
        }

        return GeneralResponse.getResponse(HttpStatus.OK, pendient);
    }

    @GetMapping("{course}/gradedAssigment")
    public ResponseEntity<GeneralResponse> getGraded(@PathVariable String course) {

        Course courseFound = courseService.findByIdentifier(course);
        User userFound = userService.findUserAuthenticated();
        List<Take> participants = courseService.findAllUsersByCourse(courseFound, userFound);

        if (courseFound == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "Course not found");
        }

        if (participants.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.CONFLICT, "You dont participate in this course");
        }


        List<SubmitResponseDTO> graded = assignmentService.findByGradedIsTrueAndUser(userFound);

        if (graded.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "All your assignments has not graded");
        }

        return GeneralResponse.getResponse(HttpStatus.OK, graded);
    }

    @GetMapping("{course}/assignments")
    public ResponseEntity<GeneralResponse> getAssignments(@PathVariable String course) {

        Course courseFound = courseService.findByIdentifier(course);
        User userFound = userService.findUserAuthenticated();
        List<Take> participants = courseService.findAllUsersByCourse(courseFound, userFound);
        Boolean isOwner = courseService.isCreator(userFound, courseFound);

        List<AssignmentResponseDTO> assignments = assignmentService.findAllAssignments(courseFound);

        if (isOwner && assignments.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "The course dont have assignments, put it on!");
        }

        if (isOwner)
            return GeneralResponse.getResponse(HttpStatus.OK, assignments);

        if (participants.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.CONFLICT, "You dont participate in this course");
        }

        if (courseFound == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "Course not found");
        }

        if (assignments.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "The course dont have assignments, Yeah!");
        }

        return GeneralResponse.getResponse(HttpStatus.OK, assignments);
    }

    @GetMapping("{course}/toGradeAssignments")
    public ResponseEntity<GeneralResponse> toGradeAssignments(@PathVariable String course) {

        Course courseFound = courseService.findByIdentifier(course);
        User owner = userService.findUserAuthenticated();
        Boolean isOwner = courseService.isCreator(owner, courseFound);
        List<Assist> isAssistant = courseService.findAllAssistantsByCourse(courseFound, owner);

        if (courseFound == null) {

            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "Not found");
        }

        if (!isOwner && isAssistant.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.FORBIDDEN, "Only the owner or the instructor can see the pendients submits");
        }

        List<ToSubmitResponseDTO> toGrade = assignmentService.findByGradedIsFalseAndCourse(courseFound);

        if (toGrade.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "All the submits are graded");
        }

        return GeneralResponse.getResponse(HttpStatus.CREATED, toGrade);
    }

    @GetMapping("{course}/courseGradedAssignments")
    public ResponseEntity<GeneralResponse> courseGradedAssignments(@PathVariable String course) {

        Course courseFound = courseService.findByIdentifier(course);
        User owner = userService.findUserAuthenticated();
        Boolean isOwner = courseService.isCreator(owner, courseFound);
        List<Assist> isAssistant = courseService.findAllAssistantsByCourse(courseFound, owner);

        if (courseFound == null) {

            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "Not found");
        }

        if (!isOwner && isAssistant.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.FORBIDDEN, "Only the owner or the instructor can see the graded submits");
        }

        List<SubmitResponseDTO> graded = assignmentService.findAllSubmitsGraded(courseFound);

        if (graded.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "All the submits are graded");
        }

        return GeneralResponse.getResponse(HttpStatus.CREATED, graded);
    }
}
