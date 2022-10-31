package efrem.datamanager.service;

import efrem.datamanager.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    @Autowired
    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public efrem.datamanager.service.Service loadServiceByDomain(String domain) throws ServiceNotFoundException {
        return serviceRepository.findServiceByDomainAndSuggestedIsFalse(domain).orElseThrow(() ->  new ServiceNotFoundException(String.format("Domain not found: %s", domain)));
    }

    public List<efrem.datamanager.service.Service> loadSuggestions(String domain) throws ServiceNotFoundException {
        return serviceRepository.findServiceByDomainAndSuggestedIsTrue(domain).orElseThrow(() -> new ServiceNotFoundException(String.format("Suggestions not found for: %d", domain)));
    }

    public void addService(efrem.datamanager.service.Service service) {
        Optional<List<efrem.datamanager.service.Service>> serviceOptional = serviceRepository.findServiceByDomain(service.getDomain());
        if (serviceOptional.isPresent()) {
            if (service.isSuggested())
                serviceRepository.save(service);
            else {
                Optional<efrem.datamanager.service.Service> serviceOptional1 = serviceRepository.findServiceByDomainAndSuggestedIsFalse(service.getDomain());
                if (serviceOptional.isPresent())
                    throw new IllegalStateException("Service already exists");
                else
                    serviceRepository.save(service);
            }
        } else {
            serviceRepository.save(service);
        }
    }
}
