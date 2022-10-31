package efrem.datamanager.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.Registration;

@Controller
@RequestMapping(path = "register")
public class RegistrationController {

    private final RegistrationService registrationService;

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public String get(Model model) {
        return "register";
    }


    @PostMapping
    @ResponseStatus(value= HttpStatus.OK)
    public String register(RegistrationRequest registrationRequest, Model model) {
        try {
            registrationService.register(registrationRequest);
            model.addAttribute("success", "Account created succesfully. Confirmation e-mail sent.");
           return get(model);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return get(model);
        }

    }

    @GetMapping(path = "confirm")
    @ResponseStatus(value = HttpStatus.OK)
    public String confirm(@RequestParam("token") String token, Model model) {
        try {
            registrationService.confirmToken(token);
            model.addAttribute("success", "Your e-mail has been confirmed.");
            return "confirm_email";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "confirm_email";
        }

    }
}
