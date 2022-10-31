package efrem.datamanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(path= "service")
public class ServiceController {

    private final ServiceService serviceService;

    @Autowired
    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping(path = "/find/{domain}")
    public List<Service> get(@PathVariable String domain) {
        try {
            return Collections.singletonList(serviceService.loadServiceByDomain(domain));
        } catch (ServiceNotFoundException e) {
            return List.of();
        }
    }

    @GetMapping(path = "/find/suggestions/{domain}")
    public List<Service> getSuggestions(@PathVariable String domain) {
        try {
            return serviceService.loadSuggestions(domain);
        } catch (ServiceNotFoundException e) {
            return List.of();
        }
    }
}
