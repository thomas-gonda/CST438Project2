package CST438Project2.com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedRequestsCannotAddGames() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("Untrusted Game")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedNonAdminRequestsCannotAddGames() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .with(httpBasic("user", "user-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("User Game")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAddGameAndItAppearsInTheAvailableGamesList() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("Reviewable Game")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/games/[0-9]+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Reviewable Game"))
                .andExpect(jsonPath("$.publisher").value("Example Publisher"))
                .andExpect(jsonPath("$.category").value("Action"))
                .andExpect(jsonPath("$.price").value(59.99));

        mockMvc.perform(get("/api/v1/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.name == 'Reviewable Game')]").isNotEmpty());
    }

    @Test
    void gameRequiresNamePublisherCategoryAndPrice() throws Exception {
        mockMvc.perform(post("/api/v1/games")
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Incomplete Game",
                                  "publisher": "Example Publisher",
                                  "category": "Action"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void availableGamesCanBeViewedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/games"))
                .andExpect(status().isOk());
    }

    private static String gameJson(String name) {
        return """
                {
                  "name": "%s",
                  "publisher": "Example Publisher",
                  "category": "Action",
                  "price": 59.99
                }
                """.formatted(name);
    }
}
