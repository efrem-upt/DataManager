package efrem.datamanager.user;

import com.nimbusds.jose.util.Pair;
import efrem.datamanager.registration.token.ConfirmationToken;
import efrem.datamanager.registration.token.ConfirmationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private static final String USER_NOT_FOUND_MSG = "User with e-mail %s not found";
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, ConfirmationTokenService confirmationTokenService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.confirmationTokenService = confirmationTokenService;
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
}
