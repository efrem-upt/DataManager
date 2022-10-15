package efrem.datamanager.user.token;

import org.hibernate.sql.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DeleteAccountTokenService {
    private final DeleteAccountTokenRepository deleteAccountTokenRepository;

    @Autowired
    public DeleteAccountTokenService(DeleteAccountTokenRepository deleteAccountTokenRepository) {
        this.deleteAccountTokenRepository = deleteAccountTokenRepository;
    }

    public Optional<DeleteAccountToken> getToken(String token) {
        return deleteAccountTokenRepository.findByToken(token);
    }

    public Optional<DeleteAccountToken> getTokenByUserEmail(String userEmail) {
        return deleteAccountTokenRepository.findByUserEmail(userEmail);
    }

    public void saveDeleteToken(DeleteAccountToken token) {
        deleteAccountTokenRepository.save(token);
    }

    @Transactional
    public void updateToken(DeleteAccountToken deleteAccountToken) {
        DeleteAccountToken token = deleteAccountTokenRepository.findByToken(deleteAccountToken.getToken()).get();
        token.setExpiresAt(deleteAccountToken.getExpiresAt());
    }

    @Transactional
    public void deleteToken(DeleteAccountToken deleteAccountToken) {
        deleteAccountTokenRepository.deleteDeleteAccountTokenByToken(deleteAccountToken.getToken());
    }
}
