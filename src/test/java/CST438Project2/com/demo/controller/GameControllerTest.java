package CST438Project2.com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import CST438Project2.com.demo.model.Game;
import CST438Project2.com.demo.repository.GameRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    @Autowired
    private GameRepository gameRepository;

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

    @Test
    void adminCanUpdateAllFieldsAndChangesPersist() throws Exception {
        Game game = gameRepository.save(new Game(
                null,
                "Before",
                "Old Publisher",
                "Action",
                new BigDecimal("20.00")
        ));

        Long id = game.getId();

        mockMvc.perform(patch("/api/v1/games/{id}", id)
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "After",
                              "publisher": "New Publisher",
                              "category": "Puzzle",
                              "price": 15.50
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("After"))
                .andExpect(jsonPath("$.publisher").value("New Publisher"))
                .andExpect(jsonPath("$.category").value("Puzzle"))
                .andExpect(jsonPath("$.price").value(15.50));

        Game updated = gameRepository.findById(id).orElseThrow();

        assertEquals("After", updated.getName());
        assertEquals("New Publisher", updated.getPublisher());
        assertEquals("Puzzle", updated.getCategory());
        assertEquals(0, new BigDecimal("15.50").compareTo(updated.getPrice()));
    }

    @Test
    void adminCanPatchOnlyPrice() throws Exception {
        Game game = gameRepository.save(new Game(
                null,
                "Keep Name",
                "Keep Publisher",
                "Action",
                new BigDecimal("20.00")
        ));

        Long id = game.getId();

        mockMvc.perform(patch("/api/v1/games/{id}", id)
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":10.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Keep Name"))
                .andExpect(jsonPath("$.price").value(10.00));
    }

    @Test
    void anonymousCannotPatchGame() throws Exception {
        mockMvc.perform(patch("/api/v1/games/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"No\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotPatchGame() throws Exception {
        mockMvc.perform(patch("/api/v1/games/1")
                        .with(httpBasic("user", "user-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"No\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingGameReturnsNotFound() throws Exception {
        mockMvc.perform(patch("/api/v1/games/999999999")
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Missing\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidPatchReturnsBadRequest() throws Exception {
        Game game = gameRepository.save(new Game(
                null,
                "Valid",
                "Publisher",
                "Action",
                new BigDecimal("20.00")
        ));

        Long id = game.getId();

        mockMvc.perform(patch("/api/v1/games/{id}", id)
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "  ",
                              "price": -1
                            }
                            """))
                .andExpect(status().isBadRequest());

        Game unchanged = gameRepository.findById(id).orElseThrow();

        assertEquals("Valid", unchanged.getName());
    }
}
