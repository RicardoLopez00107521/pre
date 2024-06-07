package org.example.preparcial.services;

import org.example.preparcial.domain.dtos.ChangePasswordDTO;
import org.example.preparcial.domain.dtos.UserRegisterDTO;
import org.example.preparcial.domain.dtos.UserResponseDTO;
import org.example.preparcial.domain.entities.Role;
import org.example.preparcial.domain.entities.Token;
import org.example.preparcial.domain.entities.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User findUserByUsernameOrEmail(String username, String email);
    User findUserByIdentifier(String identifier);

    User findByUUID(UUID uuid);

    void register(UserRegisterDTO info);
    List<UserResponseDTO> findAll();
    void changePassword(ChangePasswordDTO info);
    void deleteUser(UUID uuid);
    boolean correctPassword(User user, String password);

    // Token Management
    void cleanTokens(User user);
    Token registerToken(User user);
    Boolean validateToken(User user, String token);

    User findUserAuthenticated();

    List<Role> rolesByUser(User user);
}
