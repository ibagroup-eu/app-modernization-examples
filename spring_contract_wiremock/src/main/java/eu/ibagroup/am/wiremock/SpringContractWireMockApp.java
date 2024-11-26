package eu.ibagroup.am.wiremock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

@SpringBootApplication
@AutoConfigureWireMock
public class SpringContractWireMockApp {

    public static void main(String[] args) {
	new SpringApplication(SpringContractWireMockApp.class).run(args);
    }

}
