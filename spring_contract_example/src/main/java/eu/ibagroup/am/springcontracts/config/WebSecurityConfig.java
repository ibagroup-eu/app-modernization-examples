package eu.ibagroup.am.springcontracts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.access.server.BearerTokenServerAccessDeniedHandler;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

import eu.ibagroup.am.springcontracts.security.ReactiveJwtAuthenticationConverter;
import eu.ibagroup.am.springcontracts.security.ReactiveJwtAuthenticationManager;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Configuration
public class WebSecurityConfig {
    
    private ReactiveJwtDecoder reactiveJwtDecoder;
    
    public WebSecurityConfig(ReactiveJwtDecoder reactiveJwtDecoder) {
	super();
	this.reactiveJwtDecoder = reactiveJwtDecoder;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
	http.exceptionHandling()
		.authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
		.accessDeniedHandler(new BearerTokenServerAccessDeniedHandler());
	http
		.authorizeExchange()
			.pathMatchers( "/error" ).permitAll()
			.pathMatchers( "/actuator/**" ).permitAll()
			.anyExchange().authenticated()
		.and()
		.oauth2ResourceServer()
			.jwt().authenticationManager(reactiveAuthenticationManager());
		
	http.cors()
		.and()
			.csrf().disable()
			.httpBasic().disable();
	return http.build();
    }
    
    @Bean
    public ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter() {
	ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
	converter.setPrincipalClaimName("email");
	return converter;
    }
    
    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
	return new ReactiveJwtAuthenticationManager(reactiveJwtAuthenticationConverter(), reactiveJwtDecoder, userDetailsService());
    }
    
    /**
     *  
     *  @return bean representing dummy user details service
     */
    @Bean
    public ReactiveUserDetailsService userDetailsService() {
	var user = User.withUsername("user@company.com").password("").authorities("ROLE_USER").build();
	var admin = User.withUsername("admin@company.com").password("").authorities("ROLE_ADMIN").build();
	return new MapReactiveUserDetailsService(user, admin);
    }

}
