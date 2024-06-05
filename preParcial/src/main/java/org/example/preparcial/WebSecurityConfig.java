package org.example.preparcial;

import jakarta.servlet.http.HttpServletResponse;
import org.example.preparcial.domain.entities.User;
import org.example.preparcial.services.UserService;

import org.example.preparcial.utils.JWTTokenFilter;
import org.example.preparcial.utils.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration // Se utiliza para indicar que una clase declara uno o mas metodos Beans
@EnableWebSecurity // Permite habilitar la configuracion Wwb en una aplicacion Spring

public class WebSecurityConfig {

    private final UserService userService; // Llamamos a nuestro servicio de usuario

    private final JWTTokenFilter jwtTokenFilter; // Llamamos a nuestro Token filter

    private final PasswordEncoder passwordEncoder; // Llamamos a nuestro PasswordEncoder

    public WebSecurityConfig(UserService userService, JWTTokenFilter jwtTokenFilter, PasswordEncoder passwordEncoder) { // Poblamos nuestra clase con los metodos
        this.userService = userService;
        this.jwtTokenFilter = jwtTokenFilter;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean // Marca un método de configuración en una clase de configuración
    AuthenticationManager authenticationManagerBean(HttpSecurity http) throws Exception {  // Es responsable de validar las credenciales del usuario durante el proceso de inicio de sesión.
        AuthenticationManagerBuilder managerBuilder = http // Representa la configuración de seguridad de la aplicación en el contexto de Spring Security.
                .getSharedObject(AuthenticationManagerBuilder.class); // Metodo de HttpSecurity obtiene un objeto compartido del contexto de seguridad. Se le pasa como argumento el tipo de
                                                                      // objeto que se está solicitando

        managerBuilder
                .userDetailsService(identifier -> { // Carga los detalles de un usuario durante el proceso de autenticación.
                    User user = userService.findUserByIdentifier(identifier); // Buscamos el usuario a traves del servicio de Usuarios

                    if (user == null)
                        throw new UsernameNotFoundException("User " + identifier + " not found"); // Si no se econtro devolvemos una excepcion

                    return user; // Si lo encontro devolvemos al usuario
                })
                .passwordEncoder(passwordEncoder); // El codificador de contraseñas se utiliza para cifrar y comparar contraseñas durante el proceso de autenticación.

        return managerBuilder.build(); // Devuelve el AuthenticationManager final configurado según las especificaciones proporcionadas.
    }

    @Bean // Marca un método de configuración en una clase de configuración
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //Http login and cors disabled
        http.httpBasic(withDefaults()).csrf(AbstractHttpConfigurer::disable); // withDefaults se utiliza para configurar la autenticación básica con valores predeterminados.
                                                                              // Deshabilita CSRF (Es una vulnerabilidad)

        // Route filter
        http.authorizeHttpRequests(auth -> auth // Configuraremos la autotizacion de las solicitudes HTTP
                .requestMatchers("/api/auth/register", "/api/auth/login")// Selerccionamos las rutas deseadas que no requeriran autenticacion
                .permitAll() // Permitimos el acceso completo a las rutas sin necesidad de nada mas
                .anyRequest() // Regla de autorización global que se aplicará a todas las solicitudes HTTP no coincidentes con reglas específicas previamente definidas.
                .authenticated() // Indica que para que el usuario desea hacer solicitudes, debe de estar previamente autenticado
        );

        //Statelessness
        http.sessionManagement(managment -> managment.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Cada solicitud se manejará de forma independiente, sin referencia a sesiones anteriores.

        //Unauthorized handler
        http.exceptionHandling(handling -> handling .authenticationEntryPoint((req, res, ex) -> {
            res.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED, // Si el usuario no esta autorizado, el servicio lanzara una excepcion
                    "Auth fail!"
            );
        }));

        //JWT filter
        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class); // Agrega antes del filtro de autenticación de nombre de usuario y contraseña

        return http.build(); // Construimos la peticion
    }
}
