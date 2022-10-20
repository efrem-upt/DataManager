package efrem.datamanager.dashboard;

import efrem.datamanager.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

        UserService userService;

    @Autowired
    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
        public String getDashboard(Model model) {
            model.addAttribute("something", "Welcome to the dashboard, " + UserService.isAuthenticatedUser());
            return "dashboard";
        }
}
