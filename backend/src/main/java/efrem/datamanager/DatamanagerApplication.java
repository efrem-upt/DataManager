package efrem.datamanager;

import com.nimbusds.jose.util.Pair;
import efrem.datamanager.user.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class DatamanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatamanagerApplication.class, args);
	}

}
