package efrem.datamanager.login;

import efrem.datamanager.user.User;
import efrem.datamanager.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/login")
public class LoginController {

    private UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String login(Principal principal) {
        if (principal!=null && ((Authentication)principal).isAuthenticated()) {
            User authenticatedUser = (User) userService.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

            String redirectURL = "";
            if (authenticatedUser.hasRole("USER")) {
                redirectURL = "dashboard";
            } else if (authenticatedUser.hasRole("MODERATOR")) {
                redirectURL = "mod";
            } else if (authenticatedUser.hasRole("ADMIN")) {
                redirectURL = "admin";
            }
            return "redirect:/" + redirectURL;
        }
        return "login";
    }
}
