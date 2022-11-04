package efrem.datamanager.console;

import efrem.datamanager.service.ServiceService;
import efrem.datamanager.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsoleRestController {

    private final UserService userService;
    private final ServiceService serviceService;

    @Autowired
    public ConsoleRestController(UserService userService, ServiceService serviceService) {
        this.userService = userService;
        this.serviceService = serviceService;
    }

    @DeleteMapping(path = "console/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void delete(String email) {
        userService.deleteUserFromConsole(email);
    }
}
