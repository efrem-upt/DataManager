package efrem.datamanager.service;

import efrem.datamanager.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(path= "service")
public class ServiceController {

    private final ServiceService serviceService;
    private final UserService userService;

    @Autowired
    public ServiceController(ServiceService serviceService, UserService userService) {
        this.serviceService = serviceService;
        this.userService = userService;
    }

    @GetMapping(path = "/find/{domain}")
    public List<Service> get(@PathVariable String domain) {
        Service service = serviceService.loadServiceByDomain(domain);
        if (service != null)
            return Collections.singletonList(service);
        else
            return List.of();
    }

    @GetMapping(path = "/find/suggestions/{domain}")
    public List<Service> getSuggestions(@PathVariable String domain) {
        List<Service> service = serviceService.loadSuggestions(domain);
        if (service != null)
            return service;
        else
            return List.of();
    }

    @PostMapping(path = "/send-suggestion", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void sendSuggestion(String domain, String contactEmail) {
        serviceService.addService(domain, contactEmail, true, userService.currentAuthenticatedUser().getEmail());
    }

    @GetMapping(path = "/find/all-suggestions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Service> getAllSuggestions() {
         return serviceService.loadSuggestedServices();
    }

    @PostMapping(path = "/accept-suggestion", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void acceptSuggestion(Long id) {
        serviceService.acceptSuggestion(id);
    }

    @PostMapping(path = "/decline-suggestion", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void declineSuggestion(Long id) {
        serviceService.declineSuggestion(id);
    }


}
