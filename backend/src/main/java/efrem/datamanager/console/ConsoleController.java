package efrem.datamanager.console;

import efrem.datamanager.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConsoleController {
    UserService userService;

    @Autowired
    public ConsoleController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/console")
    public String getConsole() {
        return "console";
    }
}
