package efrem.datamanager.user.token;

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

    public int setConfirmedAt(String token) {
        return resetPasswordTokenRepository.updateConfirmedAt(
                token, LocalDateTime.now());
    }


    @Transactional
    public void updateToken(ResetPasswordToken resetPasswordToken) {
        ResetPasswordToken token = resetPasswordTokenRepository.findByToken(resetPasswordToken.getToken()).get();
        token.setExpiresAt(resetPasswordToken.getExpiresAt());
    }
}
