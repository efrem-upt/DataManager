package efrem.datamanager.home;

import efrem.datamanager.user.User;
import efrem.datamanager.user.UserRole;
import efrem.datamanager.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
public class HomeController {

    private UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("home")
    public String home() {
        return "home";
    }

    @GetMapping("/")
    public String root(Principal principal) {
        if (principal != null) {
            User user = userService.currentAuthenticatedUser();
            if (user.getUserRole().contains(UserRole.ADMIN))
                return "forward:/console";
            else if (user.getUserRole().contains(UserRole.MODERATOR))
                return "forward:/mod";
            else
                return "forward:/dashboard";
        }
        return "home";
    }
}
