package efrem.datamanager.registration.token;

import efrem.datamanager.user.token.DeleteAccountToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConfirmationTokenService {
    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    public ConfirmationTokenService(ConfirmationTokenRepository confirmationTokenRepository) {
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    public Optional<ConfirmationToken> getTokenByUserEmail(String userEmail) {
        return confirmationTokenRepository.findByUserEmail(userEmail);
    }

    public void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }


    @Transactional
    public void updateToken(ConfirmationToken confirmationToken) {
        ConfirmationToken token = confirmationTokenRepository.findByToken(confirmationToken.getToken()).get();
        token.setExpiresAt(confirmationToken.getExpiresAt());
    }


    public void deleteToken(ConfirmationToken confirmationToken) {
        confirmationTokenRepository.deleteConfirmationTokenByToken(confirmationToken.getToken());
    }
}
