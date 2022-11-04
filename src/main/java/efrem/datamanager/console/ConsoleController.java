package efrem.datamanager.console;

import efrem.datamanager.service.ServiceService;
import efrem.datamanager.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConsoleController {

    private final UserService userService;
    private final ServiceService serviceService;

    @Autowired
    public ConsoleController(UserService userService, ServiceService serviceService) {
        this.userService = userService;
        this.serviceService = serviceService;
    }

    @GetMapping("/console")
    public String getConsole(Model model) {
        model.addAttribute("isSignedIn",UserService.isAuthenticatedUser());
        model.addAttribute("user", userService.currentAuthenticatedUser());
        model.addAttribute("services", serviceService);
        return "console";
    }
}
