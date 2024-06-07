package org.example.preparcial;

import org.example.preparcial.domain.entities.Role;
import org.example.preparcial.domain.entities.User;
import org.example.preparcial.repositories.RoleRepository;
import org.example.preparcial.repositories.UserRepository;
import org.example.preparcial.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PreParcialApplication {

	public static void main(String[] args) {
		SpringApplication.run(PreParcialApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunnerRol(RoleRepository roleRepository) {
		return args -> {
			Role existRole = roleRepository.findById("SYSADMIN").orElse(null);

			if (existRole == null) {
				Role role = new Role();

				role.setRoleId("SYSADMIN");
				role.setRolName("SYSADMIN");

				roleRepository.save(role);
			}
		};
	}

	@Bean
	public CommandLineRunner commandLineRunnerUser(UserService userService, UserRepository userRepository) {
		return args -> {
			User user = userRepository.findByUsernameOrEmail("Admin@Admin.com","Admin@Admin.com")
					.orElse(null);

			if(user == null) {
				String username = "Admin";
				String userEmail = "Admin@Admin.com";
				String password = "AAb123457%";

				userService.createDefaultUser(username, userEmail, password);
			}
		};
	}
}
