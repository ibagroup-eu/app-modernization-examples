package eu.ibagroup.am.springcontracts.service;

import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;

import eu.ibagroup.am.springcontracts.dto.ContractDto;
import reactor.core.publisher.Flux;

@Service
public class ExampleService {

    public ExampleService() {
	super();
    }
    
    private ContractDto generate(int index) {
	ContractDto contract = new ContractDto();
	contract.setId(index);
	contract.setContractNumber(RandomStringUtils.randomAlphabetic(1, 20));
	contract.setCountry(RandomStringUtils.randomAlphabetic(2, 3));
	return contract;
    }
    
    public Flux<ContractDto> loadUserContracts(String userId){
	return Flux.fromStream(
		IntStream.of(0, RandomUtils.nextInt(0, 20)).boxed().map(this::generate)
	);	
    }
}
