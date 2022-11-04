package efrem.datamanager.home;

import efrem.datamanager.user.User;
import efrem.datamanager.user.UserRole;
import efrem.datamanager.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String home(Model m) {
        m.addAttribute("isSignedIn",UserService.isAuthenticatedUser());
        if (UserService.isAuthenticatedUser())
            m.addAttribute("currentUser", userService.currentAuthenticatedUser());
        return "home";
    }

    @GetMapping("/")
    public String root(Principal principal) {
        if (principal != null) {
            User user = userService.currentAuthenticatedUser();
            if (user.getUserRole().contains(UserRole.ADMIN))
                return "redirect:/console";
            else if (user.getUserRole().contains(UserRole.MODERATOR))
                return "redirect:/mod";
            else
                return "redirect:/dashboard";
        }
        return "redirect:/home";
    }
}
