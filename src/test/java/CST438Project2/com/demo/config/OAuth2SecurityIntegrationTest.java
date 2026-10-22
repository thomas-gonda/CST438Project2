package CST438Project2.com.demo.config;

import CST438Project2.com.demo.model.Game;
import CST438Project2.com.demo.repository.GameRepository;
import CST438Project2.com.demo.repository.UserRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.oauth2.enabled=true",
        "app.oauth2.issuer-uri=https://issuer.example.test",
        "app.oauth2.audience=oauth-test-api",
        "app.oauth2.hmac-secret=oauth2-test-signing-secret-32-bytes-long",
        "app.oauth2.admin-emails=admin@example.com",
        "app.auth.basic-enabled=false"
})
@AutoConfigureMockMvc
class OAuth2SecurityIntegrationTest {

    private static final String ISSUER = "https://issuer.example.test";
    private static final String SECRET = "oauth2-test-signing-secret-32-bytes-long";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void validAdminBearerTokenCanAccessAdminEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .header(HttpHeaders.AUTHORIZATION,
                                bearerToken("admin", Instant.now().plusSeconds(300)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("OAuth Admin Game")))
                .andExpect(status().isCreated());
    }

    @Test
    void authenticatedNonAdminBearerTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .header(HttpHeaders.AUTHORIZATION,
                                bearerToken("profile", Instant.now().plusSeconds(300)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("OAuth User Game")))
                .andExpect(status().isForbidden());
    }

    @Test
    void verifiedAdminEmailCanAccessAdminEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .header(HttpHeaders.AUTHORIZATION,
                                bearerToken("profile", "admin@example.com",
                                        Instant.now().plusSeconds(300)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("Auth0 Admin Game")))
                .andExpect(status().isCreated());
    }

    @Test
    void missingBearerTokenIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("Missing Token Game")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidBearerTokenIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("Invalid Token Game")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenForAnotherAuth0ApiIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .header(HttpHeaders.AUTHORIZATION,
                                bearerTokenWithAudience("admin", "another-api",
                                        Instant.now().plusSeconds(300)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("Wrong Audience Game")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredBearerTokenIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .header(HttpHeaders.AUTHORIZATION,
                                bearerToken("admin", Instant.now().minusSeconds(60)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("Expired Token Game")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicGameCatalogRemainsAccessibleWithoutBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/games"))
                .andExpect(status().isOk());
    }

    @Test
    void validBearerTokenCanCreateReviewAndProvisionEmailUser() throws Exception {
        Game game = gameRepository.save(new Game(
                null,
                "OAuth Review Game",
                "OAuth Publisher",
                "Action",
                new BigDecimal("19.99")
        ));

        mockMvc.perform(post("/api/v1/games/{gameId}/reviews", game.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                bearerToken("profile", "auth0-user@example.com",
                                        Instant.now().plusSeconds(300)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"Works with Auth0\",\"rating\":4.5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment").value("Works with Auth0"))
                .andExpect(jsonPath("$.gameId").value(game.getId()))
                .andExpect(jsonPath("$.userId").isNumber());

        org.junit.jupiter.api.Assertions.assertTrue(
                userRepository.findByEmail("auth0-user@example.com").isPresent());
    }

    @Test
    void validBearerTokenCanCreateReviewWithoutEmailClaim() throws Exception {
        Game game = gameRepository.save(new Game(
                null,
                "Subject Review Game",
                "OAuth Publisher",
                "Action",
                new BigDecimal("29.99")
        ));

        mockMvc.perform(post("/api/v1/games/{gameId}/reviews", game.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                bearerToken("profile", Instant.now().plusSeconds(300)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"Works with subject fallback\",\"rating\":4.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment").value("Works with subject fallback"))
                .andExpect(jsonPath("$.gameId").value(game.getId()))
                .andExpect(jsonPath("$.userId").isNumber());

        org.junit.jupiter.api.Assertions.assertTrue(
                userRepository.findByEmail("oauth-test-user").isPresent());
    }

    private static String bearerToken(String scope, Instant expiresAt) {
        return bearerToken(scope, null, expiresAt);
    }

    private static String bearerToken(
            String scope,
            String email,
            Instant expiresAt) {
        return bearerTokenWithAudience(scope, email, "oauth-test-api", expiresAt);
    }

    private static String bearerTokenWithAudience(
            String scope,
            String audience,
            Instant expiresAt) {
        return bearerTokenWithAudience(scope, null, audience, expiresAt);
    }

    private static String bearerTokenWithAudience(
            String scope,
            String email,
            String audience,
            Instant expiresAt) {
        SecretKey key = new SecretKeySpec(
                SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .audience(List.of(audience))
                .subject("oauth-test-user")
                .issuedAt(expiresAt.minusSeconds(60))
                .expiresAt(expiresAt)
                .claim("scope", scope);

        if (email != null) {
            claimsBuilder.claim("email", email)
                    .claim("email_verified", true);
        }

        return "Bearer " + encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claimsBuilder.build())).getTokenValue();
    }

    private static String gameJson(String name) {
        return """
                {
                  "name": "%s",
                  "publisher": "OAuth Publisher",
                  "category": "Action",
                  "price": 19.99
                }
                """.formatted(name);
    }
}
