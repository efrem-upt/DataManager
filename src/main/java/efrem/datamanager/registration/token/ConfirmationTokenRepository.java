package efrem.datamanager.registration.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    Optional<ConfirmationToken> findByToken(String token);
    Optional<ConfirmationToken> deleteConfirmationTokenByToken(String token);

    @Query("SELECT c FROM ConfirmationToken c WHERE c.user.email = ?1")
    Optional<ConfirmationToken> findByUserEmail(String email);

}
