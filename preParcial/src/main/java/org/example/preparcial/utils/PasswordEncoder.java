package org.example.preparcial.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component // Marca una clase como componente administrador
public class PasswordEncoder extends BCryptPasswordEncoder {

    public BCryptPasswordEncoder  passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
