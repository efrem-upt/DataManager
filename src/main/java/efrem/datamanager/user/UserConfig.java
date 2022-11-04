package efrem.datamanager.user;

import efrem.datamanager.service.Service;
import efrem.datamanager.service.ServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

@Configuration
public class UserConfig {
    @Bean
    CommandLineRunner commandLineRunner(UserRepository repository, ServiceRepository serviceRepository) {
        return args -> {
            UserRole userRole1 = UserRole.USER;
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            User user = new User("dragosefrem@gmail.com", bCryptPasswordEncoder.encode("test"), Set.of(UserRole.USER, UserRole.MODERATOR, UserRole.ADMIN), new HashMap<String, Boolean>());
            User user2 = new User("efremdragos@yahoo.com", bCryptPasswordEncoder.encode("test"), Collections.singleton(userRole1), new HashMap<String, Boolean>());
            repository.saveAll(List.of(user, user2));

        };
    }
}
