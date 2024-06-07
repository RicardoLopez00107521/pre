package org.example.preparcial.controllers;

import jakarta.validation.Valid;
import org.example.preparcial.domain.dtos.*;
import org.example.preparcial.domain.entities.Assist;
import org.example.preparcial.domain.entities.Course;
import org.example.preparcial.domain.entities.Take;
import org.example.preparcial.domain.entities.User;
import org.example.preparcial.services.CourseService;
import org.example.preparcial.services.UserService;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CourseController {

    private final CourseService courseService;

    private final UserService userService;

    public CourseController(final CourseService courseService, final UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    @PostMapping("/newCourse") //
    public ResponseEntity<GeneralResponse> newCourse(@RequestBody @Valid CreateCourseDTO info) {

        Course course = courseService.findByIdentifier(info.getTittle());

        if (course != null) {
            return GeneralResponse.getResponse(HttpStatus.CONFLICT, "Tittle already in use");
        }

        courseService.createCourse(info);

        return GeneralResponse.getResponse(HttpStatus.CREATED, "Course created Succesfully");
    }

    @GetMapping("/{email}/all") //
    public ResponseEntity<GeneralResponse> getAllCourses(@PathVariable String email) {

        User user = userService.findUserByIdentifier(email);

        if (user == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "User not found");
        }

        List<CourseResponseDTO> courses = courseService.findAllCourses(user);
        if (courses.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "No courses found by this creator");
        }

        return GeneralResponse.getResponse(HttpStatus.OK, courseService.findAllCourses(user));
    }

    @GetMapping("/{courseTittle}/addAssistant") //
    public ResponseEntity<GeneralResponse> addAssistant(@RequestBody @Valid AddAssistantDTO info, @PathVariable String courseTittle) {
        User user = userService.findUserByIdentifier(info.getUser());
        Course course = courseService.findByIdentifier(info.getTittle());

        List<Assist> courses = courseService.findAllAssistantsByCourse(course, user);

        if (course == null || user == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "Course or User not found");
        }

        User tokenUser = userService.findUserAuthenticated();
        Boolean isOwner = courseService.isCreator(tokenUser, course);

        if (!isOwner) {
            return GeneralResponse.getResponse(HttpStatus.CONFLICT, "Only the owner can add assistants");
        }

        if (!courses.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.CONFLICT, "The user is already an assistant of that course");
        }

        courseService.addAssistant(info);

        return GeneralResponse.getResponse(HttpStatus.CREATED, "Assist added successfully");
    }

    @GetMapping("/{tittle}/allAssistantsByCourse") //
    public ResponseEntity<GeneralResponse> getAllAssistantsCourses(@PathVariable String tittle) {

        Course course = courseService.findByIdentifier(tittle);

        if (course == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "Course not found");
        }

        List<UserResponseDTO> assistants = courseService.findAllAssistants(course);

        User tokenUser = userService.findUserAuthenticated();
        Boolean isOwner = courseService.isCreator(tokenUser, course);

        if (isOwner) {
            return GeneralResponse.getResponse(HttpStatus.OK, courseService.findAllAssistants(course));
        }

        if (assistants.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "The course doesn't have any assistants");
        }

        return GeneralResponse.getResponse(HttpStatus.OK, courseService.findAllAssistants(course));
    }

    @GetMapping("/{email}/allCoursesByAssistant") //
    public ResponseEntity<GeneralResponse> getAllCoursesAssistants(@PathVariable String email) {

        User user = userService.findUserByIdentifier(email);

        if (user == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "User not found");
        }

        List<CourseResponseDTO> assistants = courseService.findAllCoursesByAssistant(user);

        if (assistants.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "The user doesn't have any courses assistants");
        }

        return GeneralResponse.getResponse(HttpStatus.OK, courseService.findAllCoursesByAssistant(user));
    }

    @PostMapping("/joinCourse") //
    public ResponseEntity<GeneralResponse> joinCourse(@RequestBody @Valid InscribeDTO info) {

        Course course = courseService.findByIdentifier(info.getCourseTittle());
        User user = userService.findUserAuthenticated();

        Boolean userOwner = courseService.isCreator(user, course);
        List<Assist> assistants = courseService.findAllAssistantsByCourse(course, user);

        if (userOwner && !assistants.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.CONFLICT, "You are already an assistant or creator of that course");
        }

        if (course == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "Course not found");
        }

        List<Take> courses = courseService.findAllUsersByCourse(course, user);

        if (!courses.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.CONFLICT, "You already are a participant of this course");
        }

        courseService.joinCourse(info, user);

        return GeneralResponse.getResponse(HttpStatus.CREATED, "You joined the course, good luck!");
    }

    @GetMapping("/{tittleCourse}/allParticipantsByCourse") //
    public ResponseEntity<GeneralResponse> getAllParticipantsByCourses(@PathVariable String tittleCourse) {

        Course courseExist = courseService.findByIdentifier(tittleCourse);

        if (courseExist == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "Course not found");
        }

        List<UserResponseDTO> participants = courseService.findAllParticipants(courseExist);

        if (participants.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "The course doesn't have any assistants");
        }

        return GeneralResponse.getResponse(HttpStatus.OK, courseService.findAllParticipants(courseExist));
    }

    @GetMapping("/{userEmail}/allCoursesByParticipant") //
    public ResponseEntity<GeneralResponse> getAllCoursesByParticipant(@PathVariable String userEmail) {

        User userExist = userService.findUserByIdentifier(userEmail);

        if (userExist == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "User not found");
        }

        List<CourseResponseDTO> participation = courseService.findMyCoursesParticipation(userExist);

        if (participation.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "The user doesn't have any courses assistants");
        }

        return GeneralResponse.getResponse(HttpStatus.OK, courseService.findMyCoursesParticipation(userExist));
    }

    @GetMapping("/myCoursesParticipation") //
    public ResponseEntity<GeneralResponse> getMyCoursesParticipation() {

        User user = userService.findUserAuthenticated();

        List<CourseResponseDTO> participation = courseService.findMyCoursesParticipation(user);

        if (participation.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "You doesn't have any courses participation");
        }

        return GeneralResponse.getResponse(HttpStatus.OK, courseService.findMyCoursesParticipation(user));
    }
}
