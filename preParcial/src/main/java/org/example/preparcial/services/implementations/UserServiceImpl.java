package org.example.preparcial.services.implementations;

import org.example.preparcial.domain.dtos.ChangePasswordDTO;
import org.example.preparcial.domain.dtos.UserRegisterDTO;
import org.example.preparcial.domain.dtos.UserResponseDTO;
import org.example.preparcial.domain.entities.Role;
import org.example.preparcial.domain.entities.Token;
import org.example.preparcial.domain.entities.User;
import org.example.preparcial.repositories.RoleRepository;
import org.example.preparcial.repositories.TokenRepository;
import org.example.preparcial.repositories.UserRepository;
import org.example.preparcial.services.UserService;
import org.example.preparcial.utils.JWTTools;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final JWTTools jwtTools;

    private final TokenRepository tokenRepository;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, JWTTools jwtTools, TokenRepository tokenRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.jwtTools = jwtTools;
        this.tokenRepository = tokenRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public User findUserByIdentifier(String identifier) {
        return this.findUserByUsernameOrEmail(identifier, identifier);
    }

    @Override
    public User findUserByUsernameOrEmail(String username, String email) { // Hacemos una query de si el usuario esta activo, por eso sea usa repositorio
        return userRepository
                .findByActiveIsTrueAndUsernameOrEmail(username, email)
                .orElse(null);
    }

    @Override
    public User findByUUID(UUID uuid) {
        return userRepository
                .findByUserIdAndActiveIsTrue(uuid)
                .orElse(null);
    }

    //**************************************************** CRUD ****************************************************
    @Override
    public List<UserResponseDTO> findAll() {
        return userRepository
                .findAll()
                .stream()
                .map(user -> new UserResponseDTO(user.getEmail(), user.getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackOn = Exception.class) // Indica una modificacion transaccional a la base de datos (Insercion), el rollback indica si hay una excepcion se revertira
    public void register(UserRegisterDTO info) {

        User user = new User();

        Role roles = roleRepository.findByRolName("USER").get();

        user.setUsername(info.getUsername());
        user.setEmail(info.getEmail());
        user.setPassword(info.getPassword());
        user.setActive(true);
        user.setRoles(Collections.singletonList(roles));

        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackOn = Exception.class) // Indica una modificacion transaccional a la base de datos (Insercion), el rollback indica si hay una excepcion se revertira
    public void changePassword(ChangePasswordDTO info) {
        User user = findUserByIdentifier(info.getIdentifier());

        if (user != null) {
            user.setPassword(info.getNewPassword());
            userRepository.save(user);
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class) // Indica una modificacion transaccional a la base de datos (Insercion), el rollback indica si hay una excepcion se revertira
    public void deleteUser(UUID uuid) {
        User user = findByUUID(uuid);

        if (user != null) {
            user.setActive(false);
            userRepository.save(user);
        }
    }
    //**************************************************** CRUD ****************************************************

    @Override
    public boolean correctPassword(User user, String password) {
        return !user.getPassword().equals(password);
    }

    //**************************************************** Token ****************************************************
    @Override
    @Transactional(rollbackOn = Exception.class) // Indica una modificacion transaccional a la base de datos (Insercion), el rollback indica si hay una excepcion se revertira
    public Token registerToken(User user) {
        cleanTokens(user); // Llamamos a la funcion cleanTokens

        String tokenString = jwtTools.generateToken(user); // Generamos el Token
        Token token = new Token(tokenString, user); // Le pasamos el String al Token y el usuario para que lo almacene en la base

        tokenRepository.save(token); // Guardamos el token a traves del Repositorio (CRUD)

        return token; // Devolvemos el Token
    }

    @Override
    @Transactional(rollbackOn = Exception.class) // Indica una modificacion transaccional a la base de datos (Insercion), el rollback indica si hay una excepcion se revertira
    public Boolean validateToken(User user, String token) {
        cleanTokens(user); // Limpiamos los tokens
        List<Token> tokens = tokenRepository.findByUserAndActive(user, true); // Obtenemos los tokens activos por usuario

        tokens.stream() // Creamos nuestro stream
                .filter(tk -> tk.getContent().equals(token)) // Filtramos el token que recibimos y buscamos una coincidencia total con los tokens almacenados
                .findAny() // Obtenemos todos los que son iguales, sino solo devuelve null
                /*.ifPresent(tk -> { // Si hay algo ejecutamos el stream
                    tk.setActive(false); // Seteamos falso el token
                    tokenRepository.save(tk); // Modificamos el token a traves del Repositorio (CRUD)
                })*/;
        return tokens.stream().anyMatch(tk -> tk.getContent().equals(token)); // Si un token cumple con la condicion dada devolvera true, sino devolver false
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void cleanTokens(User user) {
        List<Token> tokens = tokenRepository.findByUserAndActive(user, true); // Obtenemos todos los tokens activos del usuario en forma de lista

        tokens.forEach(token -> { // Creamos unestra lambda, para cada uno de los tokens
            if (!jwtTools.verifyToken(token.getContent())) { // Si la verificacion es false
                token.setActive(false); // Seteamos el Token como false a cada uno de los tokens en la lista
                tokenRepository.save(token); // Modificamos el Token a traves del Repositorio
            }
        });
    }
    //**************************************************** Token ****************************************************

    @Override
    public User findUserAuthenticated() {
        String username = SecurityContextHolder
                .getContext() // Proporciona acceso al contexto de seguridad en la aplicación. El método getContext() devuelve el contexto de seguridad actual.
                .getAuthentication() // Devuelve el objeto Authentication que representa los detalles de la autenticación del usuario actual.
                .getName(); // Devuelve el identificador del usuario autenticado

        return userRepository.findByActiveIsTrueAndUsernameOrEmail(username, username) // Buscamos el usuario
                .orElse(null); // Sino existe devolvemos false
    }

    @Override
    public List<Role> rolesByUser(User user) {
        return List.of();
    }
}
