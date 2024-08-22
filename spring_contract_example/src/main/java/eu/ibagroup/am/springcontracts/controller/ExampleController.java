package eu.ibagroup.am.springcontracts.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.ibagroup.am.springcontracts.dto.ContractDto;
import eu.ibagroup.am.springcontracts.service.ExampleService;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping
public class ExampleController {

    private final ExampleService service;
    
    public ExampleController(ExampleService service) {
	super();
	this.service = service;
    }
    
    @GetMapping(value = "contracts", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ContractDto> getUserContracts(String userId) {
        return service.loadUserContracts(userId);
    }

}
