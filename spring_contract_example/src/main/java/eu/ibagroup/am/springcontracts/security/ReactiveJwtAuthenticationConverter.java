package eu.ibagroup.am.springcontracts.security;

import java.util.Collection;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactiveJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {
    
    private String principalClaimName;

    private Converter<Jwt, Flux<GrantedAuthority>> jwtGrantedAuthoritiesConverter = new ReactiveJwtGrantedAuthoritiesConverterAdapter(
	    new JwtGrantedAuthoritiesConverter());

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
	return this.jwtGrantedAuthoritiesConverter.convert(jwt)
			.collectList()
			.map(authorities -> buildAuthenticationToken(jwt, authorities, principalClaimName));
    }
    
    private static JwtAuthenticationToken buildAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, String principalClaimName) {
	if(StringUtils.hasText(principalClaimName)) {
	    return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString(principalClaimName));
	}
	return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Sets the {@link Converter Converter&lt;Jwt, Flux&lt;GrantedAuthority&gt;&gt;}
     * to use. Defaults to a reactive {@link JwtGrantedAuthoritiesConverter}.
     * 
     * @param jwtGrantedAuthoritiesConverter The converter
     * @see JwtGrantedAuthoritiesConverter
     */
    public void setJwtGrantedAuthoritiesConverter(
	    Converter<Jwt, Flux<GrantedAuthority>> jwtGrantedAuthoritiesConverter) {
	Assert.notNull(jwtGrantedAuthoritiesConverter, "jwtGrantedAuthoritiesConverter cannot be null");
	this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
    }

    public void setPrincipalClaimName(String principalClaimName) {
	Assert.hasText(principalClaimName, "principalClaimName cannot be empty");
	this.principalClaimName = principalClaimName;
    }

}
