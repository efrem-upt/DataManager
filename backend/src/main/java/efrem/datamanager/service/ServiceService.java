package efrem.datamanager.service;

import efrem.datamanager.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceService {

    private final UserService userService;
    private final ServiceRepository serviceRepository;

    @Autowired
    public ServiceService(UserService userService, ServiceRepository serviceRepository) {
        this.userService = userService;
        this.serviceRepository = serviceRepository;
    }

    public efrem.datamanager.service.Service loadServiceByDomain(String domain) {
        Optional<efrem.datamanager.service.Service> optionalService = serviceRepository.findServiceByDomainAndSuggestedIsFalse(domain);
        if (optionalService.isPresent())
            return optionalService.get();
        else
            return null;
    }

    public List<efrem.datamanager.service.Service> loadSuggestions(String domain) throws ServiceNotFoundException {
        return serviceRepository.findServiceByDomainAndSuggestedIsTrue(domain).orElseThrow(() -> new ServiceNotFoundException(String.format("Suggestions not found for: %d", domain)));
    }

    @Transactional
    public void addService(String domain, String contact_email, boolean isSuggested) {
        efrem.datamanager.service.Service service = new efrem.datamanager.service.Service(domain, contact_email, isSuggested);
        Optional<List<efrem.datamanager.service.Service>> serviceOptional = serviceRepository.findServiceByDomain(service.getDomain());
        if (serviceOptional.isPresent()) {
            if (service.isSuggested())
                serviceRepository.save(service);
            else {
                Optional<efrem.datamanager.service.Service> serviceOptional1 = serviceRepository.findServiceByDomainAndSuggestedIsFalse(service.getDomain());
                if (serviceOptional1.isPresent())
                    throw new IllegalStateException("Service already exists");
                else
                    serviceRepository.save(service);
            }
        } else {
            serviceRepository.save(service);
        }
        userService.currentAuthenticatedUser().getInteractions().put(domain, true);
    }
}
