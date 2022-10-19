package efrem.datamanager.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            User user = new User("dragosefrem@gmail.com", bCryptPasswordEncoder.encode("test"), Set.of(UserRole.USER), map);
            User user2 = new User("efremdragos@yahoo.com", "test2", Collections.singleton(userRole1), map2);
            repository.saveAll(List.of(user, user2));
        };
    }
}
