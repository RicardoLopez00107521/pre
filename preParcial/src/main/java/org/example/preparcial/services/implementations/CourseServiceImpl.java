package org.example.preparcial.services.implementations;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.preparcial.domain.dtos.*;
import org.example.preparcial.domain.entities.Assist;
import org.example.preparcial.domain.entities.Course;
import org.example.preparcial.domain.entities.Take;
import org.example.preparcial.domain.entities.User;
import org.example.preparcial.repositories.AssistantRepository;
import org.example.preparcial.repositories.CourseRepository;
import org.example.preparcial.repositories.TakesRepository;
import org.example.preparcial.services.CourseService;
import org.example.preparcial.services.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    private final UserService userService;

    private final AssistantRepository assistantRepository;

    private final TakesRepository takesRepository;

    public CourseServiceImpl(CourseRepository courseRepository, UserService userService,
                             AssistantRepository assistantRepository, TakesRepository takesRepository) {
        this.courseRepository = courseRepository;
        this.userService = userService;
        this.assistantRepository = assistantRepository;
        this.takesRepository = takesRepository;
    }

    @Override
    public Course findByIdentifier(String identifier) {
        return this.findByTittleOrUser(identifier, identifier);
    }

    @Override
    public Course findByTittleOrUser(String tittle, String userEmail) {

        User user = this.userService.findUserByUsernameOrEmail(userEmail, userEmail);

        return courseRepository
                .findByActiveIsTrueAndTittleOrUser(tittle, user)
                .orElse(null);
    }

    @Override
    public Course findByUUID(UUID uuid) {
        return courseRepository
                .findByCourseIdAndActiveIsTrue(uuid)
                .orElse(null);
    }

    //**************************************************** CRUD ****************************************************
    @Override
    public List<CourseResponseDTO> findAllCourses(User user) {
        return courseRepository
                .findAll()
                .stream()
                .filter(course -> course.getUser().getEmail().equals(user.getEmail()))
                .map(course -> new CourseResponseDTO(course.getTittle(), course.getDescription(), course.getUser().getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void createCourse(CreateCourseDTO info) {

        User user = userService.findUserAuthenticated(); // Obtenemos el email del creador

        Course course = new Course();
        course.setTittle(info.getTittle());
        course.setDescription(info.getDescription());
        course.setUser(user);

        courseRepository.save(course);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void changeCourseActiveness(UUID uuid) {
        Course course = findByUUID(uuid);

        if (course != null) {
            course.setActive(false);
            courseRepository.save(course);
        }
    }
    //**************************************************** CRUD ****************************************************

    //**************************************************** X Tables ****************************************************
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void addAssistant(AddAssistantDTO info) {
        User user = userService.findUserByIdentifier(info.getUser()); // Obtenemos el email del asistente
        Course course = this.findByIdentifier(info.getTittle()); // Obtenemos el titulo del curso

        Assist assist = new Assist();
        assist.setUser(user);
        assist.setCourse(course);

        assistantRepository.save(assist);
    }

    @Override
    public List<Assist> findAllAssistantsByCourse(Course course, User user) {
        return assistantRepository
                .findAll()
                .stream()
                .filter(assist -> assist.getCourse().getTittle().equals(course.getTittle()))
                .filter(assist -> assist.getUser().getEmail().equals(user.getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponseDTO> findAllAssistants(Course course) {
        return assistantRepository
                .findAll()
                .stream()
                .filter(assist -> assist.getCourse().getTittle().equals(course.getTittle()))
                .map(assist -> new UserResponseDTO(assist.getUser().getUsername(), assist.getUser().getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDTO> findAllCoursesByAssistant(User user) {
        return assistantRepository
                .findAll()
                .stream()
                .filter(assist -> assist.getUser().getEmail().equals(user.getEmail()))
                .map(assist -> new CourseResponseDTO(assist.getCourse().getTittle(), assist.getCourse().getDescription(), assist.getCourse().getUser().getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void joinCourse(InscribeDTO info, User user) {

        Course course = this.findByIdentifier(info.getCourseTittle());

        Take take = new Take();
            take.setUser(user);
            take.setCourse(course);

            takesRepository.save(take);
    }

    @Override
    public List<Take> findAllUsersByCourse(Course course, User user) {
        return takesRepository
                .findAll()
                .stream()
                .filter(take -> take.getCourse().getTittle().equals(course.getTittle()))
                .filter(take -> take.getUser().getEmail().equals(user.getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponseDTO> findAllParticipants(Course course) {
        return takesRepository
                .findAll()
                .stream()
                .filter(take -> take.getCourse().getTittle().equals(course.getTittle()))
                .map(take -> new UserResponseDTO(take.getUser().getUsername(), take.getUser().getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDTO> findMyCoursesParticipation(User user) {
        return takesRepository
                .findAll()
                .stream()
                .filter(take -> take.getUser().getEmail().equals(user.getEmail()))
                .map(take -> new CourseResponseDTO(take.getCourse().getTittle(), take.getCourse().getDescription(), take.getCourse().getUser().getEmail()))
                .collect(Collectors.toList());
    }
    //**************************************************** X Tables ****************************************************

    @Override
    public Boolean isCreator(User user, Course course) {
        return user.getEmail().equals(course.getUser().getEmail());
    }
}
