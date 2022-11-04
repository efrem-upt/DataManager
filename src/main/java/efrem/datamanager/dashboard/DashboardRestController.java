package efrem.datamanager.dashboard;

import efrem.datamanager.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardRestController {

    private final UserService userService;

    @Autowired
    public DashboardRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/dashboard/google/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public GoogleStatus getGoogleStatus() {
        if (userService.currentAuthenticatedUser().isAssociatedGoogle())
            return new GoogleStatus("<img id = \"statusImage_success\" src=\"../photos/okay-1.1s-200px.png\" alt=\"Loading spinner\" width=\"150\"  />", "<h1 id=\"infoGoogle_success\">We're loading your interactions in the background. You can return to the dashboard.</h1>");
        else
            return null;
    }
}
