package efrem.datamanager.registration;

import efrem.datamanager.user.User;
import efrem.datamanager.user.UserRole;
import efrem.datamanager.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class RegistrationService {

    private final EmailValidator emailValidator;
    private final UserService userService;

    @Autowired
    public RegistrationService(EmailValidator emailValidator, UserService userService) {
        this.emailValidator = emailValidator;
        this.userService = userService;
    }

    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());
        if (!isValidEmail) {
            throw new IllegalStateException("The entered e-mail address is not valid");
        }
        return userService.addUser(new User(request.getEmail(), request.getPassword(), UserRole.USER, new HashMap<String, Boolean>()));
    }
}
