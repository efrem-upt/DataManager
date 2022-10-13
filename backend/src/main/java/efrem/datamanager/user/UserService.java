package efrem.datamanager.user;

import efrem.datamanager.registration.email.EmailSender;
import efrem.datamanager.registration.token.ConfirmationToken;
import efrem.datamanager.registration.token.ConfirmationTokenService;
import efrem.datamanager.user.token.ResetPasswordToken;
import efrem.datamanager.user.token.ResetPasswordTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private static final String USER_NOT_FOUND_MSG = "User with e-mail %s not found";
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final ResetPasswordTokenService resetPasswordTokenService;
    private final EmailSender emailSender;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, ConfirmationTokenService confirmationTokenService, ResetPasswordTokenService resetPasswordTokenService, EmailSender emailSender) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.confirmationTokenService = confirmationTokenService;
        this.resetPasswordTokenService = resetPasswordTokenService;
        this.emailSender = emailSender;
    }


    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public String addUser(User user) {
        Optional<User> userOptional = userRepository.findUserByEmail(user.getEmail());
        if (userOptional.isPresent()) {
            Optional<ConfirmationToken> optionalConfirmationToken = confirmationTokenService.getTokenByUserEmail(user.getEmail());
            ConfirmationToken existentToken = optionalConfirmationToken.get();
            if (optionalConfirmationToken.isPresent() && existentToken.getConfirmedAt() == null) {
                if (existentToken.isExpired()) {
                    existentToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
                    existentToken.setToken(UUID.randomUUID().toString());
                    confirmationTokenService.updateToken(existentToken);
                    return existentToken.getToken();
                } else
                    throw new IllegalStateException("You must wait 15 minutes before requesting a new confirmation e-mail");
            }
            else
                throw new IllegalStateException("E-mail address is taken");
        } else {
            String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);

            String token = UUID.randomUUID().toString();
            ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), user);
            confirmationTokenService.saveConfirmationToken(confirmationToken);

            return token;
        }


    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(username).orElseThrow(() ->  new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, username)));
    }

    public int enableUser(String email) {
        return userRepository.enableUser(email);
    }

    @Transactional
    public void resetPassword(String token, ResetPasswordRequest resetPasswordRequest) {

        if (resetPasswordTokenService.getToken(token).isPresent()) {
            if (resetPasswordTokenService.getToken(token).get().getConfirmedAt() != null)
                throw new IllegalStateException("Token has been confirmed already");
            if (resetPasswordTokenService.getToken(token).get().getExpiresAt().isBefore(LocalDateTime.now()))
                throw new IllegalStateException("Token has expired");
            if (resetPasswordTokenService.getTokenByUserEmail(resetPasswordRequest.getEmail()).isPresent()) {
                if (!token.equals(resetPasswordTokenService.getTokenByUserEmail(resetPasswordRequest.getEmail()).get().getToken())) {
                    throw new IllegalStateException("Token is not valid for entered e-mail");
                }
            } else throw new IllegalStateException("Token is not valid for entered e-mail");

            ResetPasswordToken resetPasswordToken = resetPasswordTokenService.getTokenByUserEmail(resetPasswordRequest.getEmail()).get();
            if (resetPasswordToken.getExpiresAt().isBefore(LocalDateTime.now()))
                throw new IllegalStateException("Link expired. Request a new link");


            if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getNewPasswordConfirmed())) {
                throw new IllegalStateException("Entered passwords do not match");
            } else {
                String password = bCryptPasswordEncoder.encode(resetPasswordRequest.getNewPassword());
                userRepository.updatePassword(resetPasswordRequest.getEmail(), password);
                resetPasswordToken.setConfirmedAt(LocalDateTime.now());
                resetPasswordTokenService.updateToken(resetPasswordToken);
            }
        } else throw new IllegalStateException("Token is not valid for entered e-mail");


    }

    @Transactional
    public void request(String email) {
        if (!userRepository.findUserByEmail(email).isPresent()) {
            throw new IllegalStateException("E-mail address not found");
        }
        String token = UUID.randomUUID().toString();
        Optional<ResetPasswordToken> optionalResetPasswordToken = resetPasswordTokenService.getTokenByUserEmail(email);
        if (optionalResetPasswordToken.isPresent()) {
            if (optionalResetPasswordToken.get().getExpiresAt().isAfter(LocalDateTime.now()))
                throw new IllegalStateException("You must wait 15 minutes before submitting another request");
            ResetPasswordToken existentToken = optionalResetPasswordToken.get();
            existentToken.setToken(token);
            existentToken.setCreatedAt(LocalDateTime.now());
            existentToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            existentToken.setConfirmedAt(null);
            resetPasswordTokenService.updateToken(existentToken);
        } else {
            ResetPasswordToken resetPasswordToken = new ResetPasswordToken(token, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), userRepository.findUserByEmail(email).get());
            resetPasswordTokenService.saveResetToken(resetPasswordToken);
        }

        String link = "http://localhost:8080/reset-password?token=" + token;
        emailSender.setSubject("Reset your password");
        emailSender.send(email, resetEmail(email, link));
    }

    private String resetEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Reset your password</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Click the link below to reset your password: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Reset Password</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }
}
