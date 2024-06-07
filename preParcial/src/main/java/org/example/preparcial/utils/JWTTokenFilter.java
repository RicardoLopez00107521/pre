package org.example.preparcial.utils;

import org.example.preparcial.domain.entities.User;
import org.example.preparcial.services.UserService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Marca una clase como componente administrador
@Slf4j // implementación de logging en tiempo de ejecución a través de bindings.
public class JWTTokenFilter extends OncePerRequestFilter { // Esta clase se utiliza para asegurar que un filtro se ejecute solo una vez por cada solicitud HTTP en un entorno web.

    final
    JWTTools jwtTools; // Declara constantes, no puede ser modificado no sobreescrito

    final
    UserService userService;


    public JWTTokenFilter(JWTTools jwtTools, UserService userService) {
        this.jwtTools = jwtTools; // Escribimos los metodos de jwtTools
        this.userService = userService; // Escribimos el servicio de usuario
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tokenHeader = request.getHeader("Authorization"); // Pide por cabecera lo que tenga el campo Authorization
        String username = null; // Creamos un usarname vacio que sera llenado
        String token = null; // Creamos un token vacio que sera llenado

        // Formato que debe tener el token que se envia por Header
        // Authorization: Bearer <Token>
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ") && tokenHeader.length() > 7) { // Verifica que se haya enviado algo, que el token tenga el formato deseado
            token = tokenHeader.substring(7); // Extrae el token sin contar Bearer        // y que la longitud del Token sea mayor a 7, sin contar Bearer

            try {
                username = jwtTools.getUsernameFrom(token); // Tratamos de obtener el usuario al que pertenece el Token y lo guardamos en usuario
            } catch (IllegalArgumentException e) { // Si no se puede obtener el Token, lanzamos una excepcion
                System.out.println("Unable to get JWT Token"); // Mensaje de error
            } catch (ExpiredJwtException e) { // Si el Token esta expirado, lanzamos otra excepcion
                System.out.println("JWT Token has expired"); // Mensaje de error
            } catch (MalformedJwtException e) { // Si el Token no tiene el formato deseado, lanzamos otra excepcion
                System.out.println("JWT Token has malformed"); // Mensaje de error
            }
        }
        else {
            System.out.println("Bearer string not included in the header or token is empty"); // El token no tiene Bearer, o no hay token
        }

        if (username != null && token != null && SecurityContextHolder.getContext().getAuthentication() == null) { // Verifica que el usuario y el token no sea nulo, esos se llenaron en el if anterior
            User user = userService.findUserByIdentifier(username); // Utilizamos a userService para buscar        // y el otro verifica que no haya un proceso de autenticacion activa
                                                                    // al usuario del token
            if (user != null) { // Si se encontro
                Boolean tokenValidity = userService.validateToken(user, token); // Validamos el Token

                if (tokenValidity) { // Si es valido
                    // Preparing the authentication token
                    UsernamePasswordAuthenticationToken authToken // Clase proporcionada por Spring Security para representar un token de autenticación que contiene un nombre de usuario y una
                                                                  // contraseña (o simplemente el nombre de usuario en su lugar).

                            = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()); // Lo creamos con el usuario, la contrasenia null y devuelve el rol

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request) // Crea una nueva instancia de WebAuthenticationDetailsSource, se llama la instancia y
                    );                                                                   // y se le solicita la req como argumento, el devolvera la info de la solicitud

                    //This line, sets the user to security context to be handled by the filter chain
                    SecurityContextHolder // Accede al contecto actual de seguridad
                            .getContext()   // Lo devuelve
                            .setAuthentication(authToken); // Establece la autenticacion del token al contexto de seguridad
                }
                else {
                    throw new RuntimeException("Invalid token"); // Token invalido
                }
            }
        }

        filterChain.doFilter(request, response); // El recibira la req y la response de nuestro doFilterInternal
    }
}
