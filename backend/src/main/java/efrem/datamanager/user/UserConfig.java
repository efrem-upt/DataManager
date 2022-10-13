package efrem.datamanager.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;

@Configuration
public class UserConfig {
    @Bean
    CommandLineRunner commandLineRunner(UserRepository repository) {
        return args -> {
            UserRole userRole1 = UserRole.USER;
            HashMap<String, Boolean> map = new HashMap<>();
            map.put("test", true);
            map.put("blabla", false);
            HashMap<String, Boolean> map2 = new HashMap<>();
            map2.put("amazon.com",false);
            map2.put("facebook.com",false);
            User user = new User("dragosefrem@gmail.com", "test", userRole1, map);
            User user2 = new User("efremdragos@yahoo.com", "test2", userRole1, map2);
            repository.saveAll(List.of(user, user2));
        };
    }
}
