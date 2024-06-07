package org.example.preparcial.controllers;

import jakarta.validation.Valid;
import org.example.preparcial.domain.dtos.*;
import org.example.preparcial.domain.entities.Course;
import org.example.preparcial.domain.entities.Token;
import org.example.preparcial.domain.entities.User;
import org.example.preparcial.services.CourseService;
import org.example.preparcial.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<GeneralResponse> login(@RequestBody @Valid UserLoginDTO info) {

        User user = userService.findUserByIdentifier(info.getIdentifier());

        if (user == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "User not found");
        }

        if (userService.correctPassword(user, info.getPassword())) {
            return GeneralResponse.getResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        Token token = userService.registerToken(user);

        return GeneralResponse.getResponse(HttpStatus.OK, "Login successful", new TokenDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<GeneralResponse> register(@RequestBody @Valid UserRegisterDTO info) {
        User user = userService.findUserByUsernameOrEmail(info.getUsername(), info.getEmail());

        if (user != null) {
            return GeneralResponse.getResponse(HttpStatus.CONFLICT, "User already exists");
        }

        userService.register(info);

        return GeneralResponse.getResponse(HttpStatus.CREATED, "User registered successfully");
    }

    @GetMapping("/all")
    public ResponseEntity<GeneralResponse> getAll() {
        List<UserResponseDTO> users = userService.findAll();

        if (users.isEmpty()) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "Users not found");
        }
        return GeneralResponse.getResponse(HttpStatus.OK, userService.findAll());
    }

    @PostMapping("/update")
    public ResponseEntity<GeneralResponse> updateUser(@RequestBody @Valid ChangePasswordDTO info) {
        User user = userService.findUserByIdentifier(info.getIdentifier());

        if (user == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "User not found");
        }

        if (userService.correctPassword(user, info.getNewPassword())) {
            return GeneralResponse.getResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        userService.changePassword(info);

        return GeneralResponse.getResponse(HttpStatus.OK, "User updated successfully");
    }

    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<GeneralResponse> deleteUser(@PathVariable UUID id) {
        if (userService.findByUUID(id) == null) {
            return GeneralResponse.getResponse(HttpStatus.NOT_FOUND, "User not found");
        }

        userService.deleteUser(id);

        return GeneralResponse.getResponse(HttpStatus.OK, "User deleted successfully");
    }

    @GetMapping("/whoami")
    public ResponseEntity<GeneralResponse> whoami() {
        User user = userService.findUserAuthenticated();
        String userEmail = user.getEmail();

        return GeneralResponse.getResponse(HttpStatus.OK, userEmail);
    }
}
