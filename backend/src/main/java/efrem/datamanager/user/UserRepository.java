package efrem.datamanager.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// interface responsible for data access

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
