package efrem.datamanager.service;

import efrem.datamanager.user.User;
import efrem.datamanager.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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

    public List<efrem.datamanager.service.Service> loadSuggestedServices() {
        Optional<List<efrem.datamanager.service.Service>> optionalServiceList = serviceRepository.findServiceBySuggested(true);
        if (optionalServiceList.isPresent())
            return optionalServiceList.get();
        else
            return null;
    }

    public List<efrem.datamanager.service.Service> loadSuggestions(String domain) {
        Optional<List<efrem.datamanager.service.Service>> optionalServiceList = serviceRepository.findServiceByDomainAndSuggestedIsTrue(domain);
        if (optionalServiceList.isPresent())
            return optionalServiceList.get();
        else
            return null;
    }

    @Transactional
    public void addService(String domain, String contact_email, boolean isSuggested, String suggestionUserEmail) {
        efrem.datamanager.service.Service service = new efrem.datamanager.service.Service(domain, contact_email, isSuggested, suggestionUserEmail);
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

    @Async
    @Transactional
    public void acceptSuggestion(Long id) {
        Optional<efrem.datamanager.service.Service> optionalSuggestion = serviceRepository.findServiceById(id);
        efrem.datamanager.service.Service suggestion = optionalSuggestion.get();
        addService(suggestion.getDomain(), suggestion.getContact_email(), false, "");
        List<efrem.datamanager.service.Service> suggestedServices = loadSuggestedServices();
        for (efrem.datamanager.service.Service service : suggestedServices) {
            if (service.getDomain().equals(suggestion.getDomain()))
                serviceRepository.deleteServiceById(service.getId());
        }
        for (User user : userService.getAllUsers()) {
            if (user.getInteractions().containsKey(suggestion.getDomain()))
                if (user.getInteractions().get(suggestion.getDomain()) == true)
                    user.getInteractions().put(suggestion.getDomain(), false);
        }
    }

    @Async
    @Transactional
    public void declineSuggestion(Long id) {
        Optional<efrem.datamanager.service.Service> optionalSuggestion = serviceRepository.findServiceById(id);
        efrem.datamanager.service.Service suggestion = optionalSuggestion.get();
        User user = (User) userService.loadUserByUsername(suggestion.getSuggestionUserEmail());
        user.getInteractions().put(suggestion.getDomain(), false);
        serviceRepository.deleteServiceById(suggestion.getId());
    }
}
