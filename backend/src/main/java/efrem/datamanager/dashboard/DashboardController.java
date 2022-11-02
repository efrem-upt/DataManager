package efrem.datamanager.dashboard;

import efrem.datamanager.service.ServiceService;
import efrem.datamanager.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Controller
public class DashboardController {

        private final UserService userService;
        private final ServiceService serviceService;


    @Autowired
    public DashboardController(UserService userService, ServiceService serviceService) {
        this.userService = userService;
        this.serviceService = serviceService;
    }

    @GetMapping("/dashboard")
        public String getDashboard(Model model) {
            model.addAttribute("isSignedIn",UserService.isAuthenticatedUser());
            model.addAttribute("user", userService.currentAuthenticatedUser());
            model.addAttribute("services", serviceService);
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
