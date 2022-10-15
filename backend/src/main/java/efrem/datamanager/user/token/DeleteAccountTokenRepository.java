package efrem.datamanager.user.token;

import org.hibernate.sql.Delete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DeleteAccountTokenRepository extends JpaRepository<DeleteAccountToken, Long> {
    Optional<DeleteAccountToken> findByToken(String token);

    @Query("SELECT c FROM DeleteAccountToken c WHERE c.user.email = ?1")
    Optional<DeleteAccountToken> findByUserEmail(String email);
    Optional<DeleteAccountToken> deleteDeleteAccountTokenByToken(String token);

}
