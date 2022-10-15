package efrem.datamanager.user;

import efrem.datamanager.user.token.ResetPasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "request-reset-password")
    public void requestReset(@RequestParam("email") String email) {
        userService.request(email);
    }

    @PostMapping(path = "reset-password/{token}")
    public void resetPassword(@PathVariable("token") String token, @RequestBody ResetPasswordRequest resetPasswordRequest) {
        userService.resetPassword(token, resetPasswordRequest);
    }

    @PostMapping(path = "request-delete-account")
    public void requestDelete(@RequestBody DeleteUserRequest deleteUserRequest) {
        userService.requestDelete(deleteUserRequest.getPassword());
    }

    @DeleteMapping(path = "delete/{token}")
    public void delete(@PathVariable("token") String token) {
        userService.deleteUser(token);
    }
}
