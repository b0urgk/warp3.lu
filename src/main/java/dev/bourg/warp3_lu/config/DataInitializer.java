package dev.bourg.warp3_lu.config;

import dev.bourg.warp3_lu.model.User;
import dev.bourg.warp3_lu.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;


@Configuration
public class DataInitializer {

    @Bean
    @Transactional
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder){
        return args -> {
            if(userRepository.count() == 0){
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@warp3.lu");
                admin.setPassword(passwordEncoder.encode("ChangeMe123"));
                admin.setRole(User.Role.ADMIN);

                userRepository.save(admin);

                System.out.println("=================================");
                System.out.println("Default admin user created:");
                System.out.println("Username: admin");
                System.out.println("Password: admin123");
                System.out.println("=================================");
            }
        };
    }

}
