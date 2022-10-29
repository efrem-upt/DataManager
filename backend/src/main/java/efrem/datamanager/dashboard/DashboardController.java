package efrem.datamanager.dashboard;

import efrem.datamanager.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Controller
public class DashboardController {

        UserService userService;

    @Autowired
    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
        public String getDashboard(Model model) {
            model.addAttribute("isSignedIn",UserService.isAuthenticatedUser());
            model.addAttribute("user", userService.currentAuthenticatedUser());
            return "dashboard";
        }
    @GetMapping("/dashboard/google")
        public String getGoogle(Model model) throws GeneralSecurityException, IOException {
            model.addAttribute("isSignedIn",UserService.isAuthenticatedUser());
            model.addAttribute("user", userService.currentAuthenticatedUser());
            userService.getInteractionsFromGoogle();
            return "google";
    }

}
