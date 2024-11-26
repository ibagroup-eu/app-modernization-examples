package eu.ibagroup.am.springcontracts.contracts;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import io.restassured.RestAssured;
import reactor.core.publisher.Mono;

public abstract class SpringContractsBase {
    
    private final String corporateEmailPostfix = "@company.com";

    @MockBean
    ReactiveJwtDecoder jwtDecoder;
    
    protected void mockDecoder() {
	when(jwtDecoder.decode(any())).then(answer -> {
		Map<String, Object> claims = new HashMap<>();
		String userId = Optional.of(answer.getArgument(0, String.class)).orElse("");
		if (!userId.endsWith(corporateEmailPostfix)) {
		    userId += corporateEmailPostfix; 
		}
		claims.putAll(Collections.singletonMap("email", userId));
		Jwt jwt = new Jwt("token", 
			Instant.now(), 
			Instant.now().plusSeconds(60),
			Collections.singletonMap("key", "value"),
			claims
		);
		return Mono.just(jwt);	
	});
    }
    
    protected void initRestAssured(int port) {
	RestAssured.baseURI = "http://localhost:" + port;
    }

}
