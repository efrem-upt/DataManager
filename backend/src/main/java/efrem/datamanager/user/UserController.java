package efrem.datamanager.user;

import efrem.datamanager.user.token.ResetPasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.awt.*;
import java.io.IOException;
import java.util.List;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "forgot-password")
    public String forgotPassword(Model model) {
        return "forgot-password";
    }

    @PostMapping(path = "request-reset-password")
    public String requestReset(String email, Model model) {
        try {
            userService.request(email);
            model.addAttribute("success", "A password reset link has been sent to your e-mail");
            return forgotPassword(model);
        } catch(IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return forgotPassword(model);
        }

    }

    @GetMapping(path = "reset-password/{token}")
    public String getResetPassword(Model model) {
        return "reset-password";
    }

    @PostMapping(path = "reset-password/{token}")
    public String resetPassword(@PathVariable("token") String token, ResetPasswordRequest resetPasswordRequest, Model model) {
        try {
            userService.resetPassword(token, resetPasswordRequest);
            model.addAttribute("success", "Your password has been changed");
            return getResetPassword(model);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return getResetPassword(model);
        }

    }

    @PostMapping(path = "/user/request-delete-account")
    public void requestDelete(@RequestBody DeleteUserRequest deleteUserRequest) {
        userService.requestDelete(deleteUserRequest.getPassword());
    }

    @DeleteMapping(path = "/user/delete/{token}")
    public void delete(@PathVariable("token") String token) {
        userService.deleteUser(token);
    }

    @PostMapping(path = "/user/send-email", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void send(String domain, String email) throws MessagingException, IOException {
        userService.sendEmailToRemoveService(domain, email);
    }
}
