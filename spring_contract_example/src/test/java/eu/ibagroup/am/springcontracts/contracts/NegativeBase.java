package eu.ibagroup.am.springcontracts.contracts;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "server.port=0")
public class NegativeBase extends SpringContractsBase{

    @Value("${local.server.port:8080}")
    private int port;
    
    @BeforeAll
    public void init() {
	mockDecoder();
	initRestAssured(port);
    }

}
