package CST438Project2.com.demo.config;

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
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.oauth2.enabled=true",
        "app.oauth2.issuer-uri=https://issuer.example.test",
        "app.oauth2.hmac-secret=oauth2-test-signing-secret-32-bytes-long"
})
@AutoConfigureMockMvc
class OAuth2SecurityIntegrationTest {

    private static final String ISSUER = "https://issuer.example.test";
    private static final String SECRET = "oauth2-test-signing-secret-32-bytes-long";

    @Autowired
    private MockMvc mockMvc;

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

    private static String bearerToken(String scope, Instant expiresAt) {
        SecretKey key = new SecretKeySpec(
                SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject("oauth-test-user")
                .issuedAt(expiresAt.minusSeconds(60))
                .expiresAt(expiresAt)
                .claim("scope", scope)
                .build();

        return "Bearer " + encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
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
