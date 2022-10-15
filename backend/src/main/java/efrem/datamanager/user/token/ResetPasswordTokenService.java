package efrem.datamanager.user.token;

import efrem.datamanager.registration.token.ConfirmationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ResetPasswordTokenService {
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;

    @Autowired
    public ResetPasswordTokenService(ResetPasswordTokenRepository resetPasswordTokenRepository) {
        this.resetPasswordTokenRepository = resetPasswordTokenRepository;
    }

    public Optional<ResetPasswordToken> getToken(String token) {
        return resetPasswordTokenRepository.findByToken(token);
    }

    public Optional<ResetPasswordToken> getTokenByUserEmail(String userEmail) {
        return resetPasswordTokenRepository.findByUserEmail(userEmail);
    }

    public void saveResetToken(ResetPasswordToken token) {
        resetPasswordTokenRepository.save(token);
    }


    @Transactional
    public void updateToken(ResetPasswordToken resetPasswordToken) {
        ResetPasswordToken token = resetPasswordTokenRepository.findByToken(resetPasswordToken.getToken()).get();
        token.setExpiresAt(resetPasswordToken.getExpiresAt());
    }

    public void deleteToken(ResetPasswordToken resetPasswordToken) {
        resetPasswordTokenRepository.deleteResetPasswordTokenByToken(resetPasswordToken.getToken());
    }
}
