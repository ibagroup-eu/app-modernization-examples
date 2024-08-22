package eu.ibagroup.am.springcontracts.security;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.Assert;

import reactor.core.publisher.Mono;

public class ReactiveJwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final ReactiveJwtDecoder jwtDecoder;

    private final ReactiveJwtAuthenticationConverter jwtAuthenticationConverter;
    
    private final ReactiveUserDetailsService userDetailsService;
    
    public ReactiveJwtAuthenticationManager(
	    ReactiveJwtAuthenticationConverter jwtAuthenticationConverter,
	    ReactiveJwtDecoder jwtDecoder,
	    ReactiveUserDetailsService userDetailsService) {
	Assert.notNull(jwtDecoder, "jwtDecoder cannot be null");
	Assert.notNull(jwtAuthenticationConverter, "jwtAuthenticationConverter cannot be null");
	this.jwtAuthenticationConverter = jwtAuthenticationConverter;
	this.jwtDecoder = jwtDecoder;
	this.userDetailsService = userDetailsService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
	return Mono.justOrEmpty(authentication)
		.filter(a -> a instanceof BearerTokenAuthenticationToken)
		.switchIfEmpty(Mono.error(new InvalidBearerTokenException("Bearer token is expected")))
		.cast(BearerTokenAuthenticationToken.class)
		.map(BearerTokenAuthenticationToken::getToken)
		.flatMap(jwtDecoder::decode)
		.flatMap(jwtAuthenticationConverter::convert)
		.cast(JwtAuthenticationToken.class)
		.flatMap(jwtAuthentication -> 
			userDetailsService.findByUsername(jwtAuthentication.getName())
				.switchIfEmpty(Mono.error(new UsernameNotFoundException(jwtAuthentication.getName())))
				.map(userInfo -> authorizeIt(jwtAuthentication, userInfo))
		)
		.cast(Authentication.class)
		.onErrorMap(Exception.class, this::onError);
    }
    
    private OAuth2AuthenticationToken authorizeIt(JwtAuthenticationToken authorization, UserDetails userInfo) {
	Map<String, Object> attributes = new LinkedHashMap<>(authorization.getTokenAttributes());
	OAuth2User oauthUser = new DefaultOAuth2User(userInfo.getAuthorities(), attributes, "email");
	return new OAuth2AuthenticationToken(oauthUser, userInfo.getAuthorities(), "example_app");
    }
    
    private AuthenticationException onError(Exception ex) {
	if (ex instanceof JwtException) {
	    return new InvalidBearerTokenException("Authentication error:", ex);
	}
	if (ex instanceof AuthenticationException) {
	    return (AuthenticationException) ex;
	}
	return new AuthenticationServiceException("Authentication error:", ex);
    }

}
