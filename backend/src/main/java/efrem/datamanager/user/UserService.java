package efrem.datamanager.user;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import efrem.datamanager.gmail.GmailQuickstart;
import efrem.datamanager.registration.email.EmailSender;
import efrem.datamanager.registration.token.ConfirmationToken;
import efrem.datamanager.registration.token.ConfirmationTokenService;
import efrem.datamanager.service.ServiceService;
import efrem.datamanager.user.token.DeleteAccountToken;
import efrem.datamanager.user.token.DeleteAccountTokenService;
import efrem.datamanager.user.token.ResetPasswordToken;
import efrem.datamanager.user.token.ResetPasswordTokenService;
import efrem.datamanager.user.validator.StrongPasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ui.Model;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
public class UserService implements UserDetailsService {

    private static final String USER_NOT_FOUND_MSG = "User with e-mail %s not found";
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final ResetPasswordTokenService resetPasswordTokenService;
    private final EmailSender emailSender;
    private final DeleteAccountTokenService deleteAccountTokenService;
    private final ServiceService serviceService;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    TransactionManager transactionManager;

    @PersistenceContext
    private EntityManager em;


    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, ConfirmationTokenService confirmationTokenService, ResetPasswordTokenService resetPasswordTokenService, EmailSender emailSender, DeleteAccountTokenService deleteAccountTokenService, ServiceService serviceService, TransactionTemplate transactionTemplate) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.confirmationTokenService = confirmationTokenService;
        this.resetPasswordTokenService = resetPasswordTokenService;
        this.emailSender = emailSender;
        this.deleteAccountTokenService = deleteAccountTokenService;
        this.serviceService = serviceService;
        this.transactionTemplate = transactionTemplate;
    }


    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public String addUser(User user) {
        Optional<User> userOptional = userRepository.findUserByEmail(user.getEmail());
        if (userOptional.isPresent()) {
            /*
            Optional<ConfirmationToken> optionalConfirmationToken = confirmationTokenService.getTokenByUserEmail(user.getEmail());
            if (optionalConfirmationToken.isPresent()) {
                ConfirmationToken existentToken = optionalConfirmationToken.get();
                if (existentToken.isExpired()) {
                    existentToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
                    existentToken.setToken(UUID.randomUUID().toString());
                    confirmationTokenService.updateToken(existentToken);
                    return existentToken.getToken();
                } else
                    throw new IllegalStateException("You must wait 15 minutes before requesting a new confirmation e-mail");
            }
             */
                throw new IllegalStateException("E-mail address is taken");
        } else {
            String password = user.getPassword();
            String passwordErrorMessage = StrongPasswordValidator.result(password);
            if (passwordErrorMessage.isEmpty()) {
                String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
                user.setPassword(encodedPassword);
                userRepository.save(user);

                String token = UUID.randomUUID().toString();
                ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), user);
                confirmationTokenService.saveConfirmationToken(confirmationToken);

                return token;
            } else {
                throw new IllegalStateException(passwordErrorMessage);
            }
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
            if (resetPasswordTokenService.getToken(token).get().getExpiresAt().isBefore(LocalDateTime.now()))
                throw new IllegalStateException("Token has expired");

            if (resetPasswordTokenService.getTokenByUserEmail(resetPasswordRequest.getEmail()).isPresent()) {
                if (!token.equals(resetPasswordTokenService.getTokenByUserEmail(resetPasswordRequest.getEmail()).get().getToken())) {
                    throw new IllegalStateException("Token is not valid for entered e-mail");
                }
            } else throw new IllegalStateException("Token is not valid for entered e-mail");

            if (resetPasswordRequest.getNewPassword() == null)
                throw new IllegalStateException("Password must not be empty");
            if (resetPasswordRequest.getNewPasswordConfirmed() == null)
                throw new IllegalStateException("Confirmed password must not be empty");

            ResetPasswordToken resetPasswordToken = resetPasswordTokenService.getTokenByUserEmail(resetPasswordRequest.getEmail()).get();
            if (resetPasswordToken.getExpiresAt().isBefore(LocalDateTime.now()))
                throw new IllegalStateException("Link expired. Request a new link");


            if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getNewPasswordConfirmed())) {
                throw new IllegalStateException("Entered passwords do not match");
            } else {
                String password = bCryptPasswordEncoder.encode(resetPasswordRequest.getNewPassword());
                userRepository.updatePassword(resetPasswordRequest.getEmail(), password);
                resetPasswordTokenService.deleteToken(resetPasswordToken);
            }
        } else throw new IllegalStateException("Token is not valid for entered e-mail");


    }

    @Transactional
    public void request(String email) {
        if (!userRepository.findUserByEmail(email).isPresent()) {
            throw new IllegalStateException("E-mail address not found");
        }
        if (!userRepository.findUserByEmail(email).get().getEnabled()) {
            throw new IllegalStateException("Account must be activated before requesting password reset");
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
            resetPasswordTokenService.updateToken(existentToken);
        } else {
            ResetPasswordToken resetPasswordToken = new ResetPasswordToken(token, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), userRepository.findUserByEmail(email).get());
            resetPasswordTokenService.saveResetToken(resetPasswordToken);
        }

        String link = "http://localhost:8080/reset-password/" + token;
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

    public User currentAuthenticatedUser() {
        return (User) loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    public static boolean isAuthenticatedUser() {
       return  SecurityContextHolder.getContext().getAuthentication() != null &&
               SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
               //when Anonymous Authentication is enabled
               !(SecurityContextHolder.getContext().getAuthentication()
                       instanceof AnonymousAuthenticationToken);
    }

    @Transactional
    public void deleteUser(String token) {
        User authenticatedUser = (User) loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Optional<DeleteAccountToken> optionalDeleteAccountToken = deleteAccountTokenService.getToken(token);
        if (optionalDeleteAccountToken.isPresent()) {
            DeleteAccountToken deleteAccountToken = optionalDeleteAccountToken.get();
            if (deleteAccountToken.isExpired())
                throw new IllegalStateException("Token has expired");

            Optional<DeleteAccountToken> optionalEmailToken = deleteAccountTokenService.getTokenByUserEmail(authenticatedUser.getEmail());
            if (optionalEmailToken.isPresent()) {
                DeleteAccountToken deleteAccountTokenEmail = optionalEmailToken.get();
                if (deleteAccountToken == deleteAccountTokenEmail) {
                    String email = authenticatedUser.getEmail();
                    userRepository.deleteUserByEmail(authenticatedUser.getEmail());
                    emailSender.setSubject("Your account has been deleted");
                    emailSender.send(email, deletedAccountEmail(email, ""));

                } else throw new IllegalStateException("Token doesn't match the current user");
            } else
                throw new IllegalStateException("Token doesn't match the current user");
        } else throw new IllegalStateException("Token doesn't match the current user");
    }

    @Transactional
    public void requestDelete(String password) {
        User authenticatedUser = (User) loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Optional<DeleteAccountToken> optionalDeleteAccountToken = deleteAccountTokenService.getTokenByUserEmail(authenticatedUser.getEmail());
        if (optionalDeleteAccountToken.isPresent()) {
            DeleteAccountToken deleteAccountToken = optionalDeleteAccountToken.get();
            if (!deleteAccountToken.isExpired()) {
                throw new IllegalStateException("You must wait 15 minutes before submitting another request");
            } else {
                deleteAccountToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
                String newToken = UUID.randomUUID().toString();
                deleteAccountToken.setToken(newToken);
                emailSender.setSubject("Complete the process of deleting your account");
                String link = "http://localhost:8080/user/delete/" + newToken;
                emailSender.send(authenticatedUser.getEmail(), deleteAccountEmail(authenticatedUser.getUsername(), link));
                return;
            }
        }
        if (bCryptPasswordEncoder.matches(password, authenticatedUser.getPassword())) {
            String token = UUID.randomUUID().toString();
            DeleteAccountToken deleteAccountToken = new DeleteAccountToken(token, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), authenticatedUser);
            deleteAccountTokenService.saveDeleteToken(deleteAccountToken);
            emailSender.setSubject("Complete the process of deleting your account");
            String link = "http://localhost:8080/user/delete/" + token;
            emailSender.send(authenticatedUser.getEmail(), deleteAccountEmail(authenticatedUser.getUsername(), link));
        } else
            throw new IllegalStateException("The entered password is incorrect");
        }

    private String deleteAccountEmail(String name, String link) {
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
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Delete your account</span>\n" +
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
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Click the link below to delete your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Delete Account</a> </p></blockquote>\n Link will expire in 15 minutes. <p>We're sorry to see you go</p>" +
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

    private String deletedAccountEmail(String name, String link) {
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
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Account successfully deleted</span>\n" +
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
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">You've succesfully deleted your account. This action is permanent and all your data has been deleted from our systems.</blockquote>\n You can always create a new account to start over again. <p>We're sorry to see you go</p>" +
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

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "DataManager";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = List.of(GmailScopes.GMAIL_SEND, GmailScopes.GMAIL_METADATA);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        User currentAuthenticatedUser = currentAuthenticatedUser();
        // Load client secrets.
        InputStream in = GmailQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8081).build();
        Credential credential = new GoogleAuthorizationCodeInstalledApp(flow, receiver).authorize(currentAuthenticatedUser.getEmail());
        //returns an authorized Credential object.
        return credential;
    }

    @Async
    public void getInteractionsFromGoogle() throws IOException, GeneralSecurityException {
        User currentAuthenticatedUser = currentAuthenticatedUser();
        if (currentAuthenticatedUser.isAssociatedGoogle())
            return;
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ListMessagesResponse response = service.users().messages().list(currentAuthenticatedUser.getEmail()).execute();
        while (response.getMessages() != null) {
            ListMessagesResponse finalResponse = response;
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        doSomething(finalResponse, service, currentAuthenticatedUser);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users().messages().list(currentAuthenticatedUser.getEmail())
                        .setPageToken(pageToken).execute();
            } else {
                break;
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void doSomething(ListMessagesResponse response, Gmail service, User currentAuthenticatedUser) throws IOException {
        if (!currentAuthenticatedUser.isAssociatedGoogle())
            currentAuthenticatedUser.setAssociatedGoogle(true);
        for (Message message : response.getMessages()) {
            Message m1 = service.users().messages().get(currentAuthenticatedUser.getEmail(), message.getId()).setFormat("metadata").setFields("payload/headers").execute();
            Stream<String> fromHeaderValue = m1.getPayload().getHeaders().stream()
                    .filter(h -> "From".equals(h.getName())).map(h -> h.getValue());
            String emailAddress = fromHeaderValue.toArray(String[]::new)[0];
            emailAddress = emailAddress.substring(emailAddress.indexOf("<") + 1);
            emailAddress = emailAddress.substring(0, emailAddress.length() - 1);
            emailAddress = emailAddress.substring(emailAddress.indexOf("@") + 1);
            while (emailAddress.indexOf(".") != emailAddress.lastIndexOf(".")) {
                emailAddress = emailAddress.substring(emailAddress.indexOf(".") + 1);
            }
            if (!emailAddress.equals("gmail.com") && !emailAddress.equals("yahoo.com") && !emailAddress.equals("outlook.com")
            && !emailAddress.equals("hotmail.com") && !emailAddress.contains("upt.ro")) {
                currentAuthenticatedUser.addInteraction(emailAddress, false);
                userRepository.saveAndFlush(currentAuthenticatedUser);
            }
        }
    }

}