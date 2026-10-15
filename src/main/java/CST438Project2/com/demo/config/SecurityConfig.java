package CST438Project2.com.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            @Value("${app.oauth2.enabled:false}") boolean oauth2Enabled,
            @Value("${app.auth.basic-enabled:true}") boolean basicAuthEnabled) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> {
                    authorize
                            .requestMatchers(HttpMethod.GET,
                                    "/api/v1/games",
                                    "/api/v1/games/*",
                                    "/api/v1/games/*/reviews",
                                    "/api/v1/reviews/*",
                                    "/hello")
                            .permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/games")
                            .hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PATCH, "/api/v1/games/*")
                            .hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/v1/games/*")
                            .hasRole("ADMIN");

                    if (!oauth2Enabled) {
                        authorize.requestMatchers(HttpMethod.PATCH,
                                "/api/v1/reviews/*").permitAll();
                    }

                    if (oauth2Enabled) {
                        authorize.anyRequest().authenticated();
                    } else {
                        authorize.anyRequest().permitAll();
                    }
                });

        if (basicAuthEnabled) {
            http.httpBasic(httpBasic -> { });
        }

        if (oauth2Enabled) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(
                            jwtAuthenticationConverter)));
        }

        return http.build();
    }

    /**
     * Converts standard OAuth2 scopes and common role claims into the
     * authorities used by the existing endpoint authorization rules.
     *
     * @param adminScope the OAuth2 scope that grants administrator access
     * @return the JWT authentication converter
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(
            @Value("${app.oauth2.admin-scope:admin}") String adminScope,
            @Value("${app.oauth2.admin-emails:}") String adminEmails) {
        JwtGrantedAuthoritiesConverter scopeConverter =
                new JwtGrantedAuthoritiesConverter();
        String adminAuthority = "SCOPE_" + adminScope.trim();
        Set<String> adminEmailSet = Arrays.stream(adminEmails.split(","))
                .map(String::trim)
                .filter(email -> !email.isBlank())
                .map(email -> email.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> scopeAuthorities =
                    scopeConverter.convert(jwt);
            ArrayList<GrantedAuthority> authorities = new ArrayList<>(
                    scopeAuthorities == null ? List.of() : scopeAuthorities);

            if (authorities.stream().anyMatch(authority ->
                    authority.getAuthority().equals(adminAuthority))) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }

            addRoleAuthorities(jwt, authorities, "roles");
            addRoleAuthorities(jwt, authorities, "role");

            String email = jwt.getClaimAsString("email");
            Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
            if (Boolean.TRUE.equals(emailVerified)
                    && email != null
                    && adminEmailSet.contains(email.toLowerCase(Locale.ROOT))) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }

            return authorities;
        });
        return converter;
    }

    /**
     * Creates a JWT decoder only when OAuth2 is enabled. Production deployments
     * should use an issuer or JWK set URI; the HMAC option supports local/test
     * environments without requiring a network call to an identity provider.
     *
     * @param issuerUri OAuth2 issuer URI
     * @param audience expected OAuth2 audience
     * @param jwkSetUri OAuth2 JWK set URI
     * @param hmacSecret optional signing secret for local/test use
     * @return a decoder that validates the configured token source
     */
    @Bean
    @ConditionalOnProperty(name = "app.oauth2.enabled", havingValue = "true")
    JwtDecoder jwtDecoder(
            @Value("${app.oauth2.issuer-uri:}") String issuerUri,
            @Value("${app.oauth2.audience:}") String audience,
            @Value("${app.oauth2.jwk-set-uri:}") String jwkSetUri,
            @Value("${app.oauth2.hmac-secret:}") String hmacSecret) {
        if (!hmacSecret.isBlank()) {
            SecretKey key = new SecretKeySpec(
                    hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();
            decoder.setJwtValidator(jwtValidator(issuerUri, audience));
            return decoder;
        }

        if (!jwkSetUri.isBlank()) {
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                    .build();
            decoder.setJwtValidator(jwtValidator(issuerUri, audience));
            return decoder;
        }

        if (!issuerUri.isBlank()) {
            NimbusJwtDecoder decoder = (NimbusJwtDecoder)
                    JwtDecoders.fromIssuerLocation(issuerUri);
            decoder.setJwtValidator(jwtValidator(issuerUri, audience));
            return decoder;
        }

        throw new IllegalStateException(
                "OAuth2 is enabled but no issuer URI, JWK set URI, or JWT secret is configured");
    }

    private static OAuth2TokenValidator<Jwt> jwtValidator(
            String issuerUri,
            String audience) {
        OAuth2TokenValidator<Jwt> issuerValidator = issuerUri.isBlank()
                ? JwtValidators.createDefault()
                : JwtValidators.createDefaultWithIssuer(issuerUri);
        if (audience.isBlank()) {
            return issuerValidator;
        }

        OAuth2TokenValidator<Jwt> audienceValidator = token ->
                token.getAudience().contains(audience)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error(
                                "invalid_token",
                                "The required audience is missing",
                                null));
        return new DelegatingOAuth2TokenValidator<>(
                issuerValidator, audienceValidator);
    }

    private static void addRoleAuthorities(
            Jwt jwt,
            Collection<GrantedAuthority> authorities,
            String claimName) {
        Object claim = jwt.getClaims().get(claimName);
        if (claim instanceof Collection<?> values) {
            values.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .forEach(role -> addRoleAuthority(authorities, role));
        } else if (claim instanceof String role) {
            addRoleAuthority(authorities, role);
        }
    }

    private static void addRoleAuthority(
            Collection<GrantedAuthority> authorities,
            String role) {
        String normalizedRole = role.toUpperCase(Locale.ROOT).startsWith("ROLE_")
                ? role.toUpperCase(Locale.ROOT)
                : "ROLE_" + role.toUpperCase(Locale.ROOT);
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(normalizedRole);
        if (!authorities.contains(authority)) {
            authorities.add(authority);
        }
    }

    @Bean
    InMemoryUserDetailsManager users(
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.admin-username}") String adminUsername,
            @Value("${app.auth.admin-password}") String adminPassword,
            @Value("${app.auth.user-username}") String userUsername,
            @Value("${app.auth.user-password}") String userPassword) {
        UserDetails admin = User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build();
        UserDetails user = User.withUsername(userUsername)
                .password(passwordEncoder.encode(userPassword))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
