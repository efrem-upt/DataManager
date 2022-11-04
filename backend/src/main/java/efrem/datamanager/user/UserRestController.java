package efrem.datamanager.user;

import efrem.datamanager.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserRestController {

    private final UserService userService;
    private final ServiceService serviceService;

    @Autowired
    public UserRestController(UserService userService, ServiceService serviceService) {
        this.userService = userService;
        this.serviceService = serviceService;
    }

    @GetMapping(path = "user/get-interactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Interactions> getInteractions() {
        List<Interactions> list = userService.currentAuthenticatedUser().getInteractions().entrySet().stream()
                .map((e)->new Interactions(e.getKey(),e.getValue(), serviceService.loadServiceByDomain(e.getKey()) == null))
                .collect(Collectors.toList());
        return list;
    }

    @GetMapping(path = "user/possible-action/{domain}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PossibleAction getPossibleAction(@PathVariable("domain") String domain) {
        PossibleAction possibleAction = new PossibleAction();
        Boolean actionTaken = userService.currentAuthenticatedUser().getInteractions().get(domain);
        if (actionTaken != null && actionTaken.booleanValue() == true)
            possibleAction.setAction("None");
        else if (serviceService.loadServiceByDomain(domain) != null)
            possibleAction.setAction("Send");
        else
            possibleAction.setAction("Suggest");
        return possibleAction;
    }
}
