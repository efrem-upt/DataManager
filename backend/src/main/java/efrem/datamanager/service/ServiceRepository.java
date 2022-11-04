package efrem.datamanager.service;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    Optional<Service> findServiceByDomainAndSuggestedIsFalse(String domain);
    Optional<List<Service>> findServiceByDomain(String domain);
    Optional<List<Service>> findServiceByDomainAndSuggestedIsTrue(String domain);
    Optional<List<Service>> findServiceBySuggested(boolean suggested);
    Optional<Service> findServiceById(Long id);
    Optional<Service> deleteServiceById(Long id);
}
