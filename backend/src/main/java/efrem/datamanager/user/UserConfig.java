package efrem.datamanager.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

@Configuration
public class UserConfig {
    @Bean
    CommandLineRunner commandLineRunner(UserRepository repository) {
        return args -> {
            UserRole userRole1 = UserRole.USER;
            SortedMap<String, Boolean> map = new TreeMap<String, Boolean>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }

            });
            TreeMap<String, Boolean> map2 = new TreeMap<>();
            map2.put("amazon.com",false);
            map2.put("facebook.com",false);
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            User user = new User("dragosefrem@gmail.com", bCryptPasswordEncoder.encode("test"), Set.of(UserRole.USER, UserRole.ADMIN), map);
            User user2 = new User("efremdragos@yahoo.com", bCryptPasswordEncoder.encode("test"), Collections.singleton(userRole1), map2);
            repository.saveAll(List.of(user, user2));
        };
    }
}
