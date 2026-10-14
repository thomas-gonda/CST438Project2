package CST438Project2.com.demo.controller;

import CST438Project2.com.demo.model.Game;
import CST438Project2.com.demo.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:game_details_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always"
})
@AutoConfigureMockMvc
@Transactional
class GameDetailsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void gameDetailsCanBeViewedWithoutLoggingIn() throws Exception {
        Game saved = gameRepository.save(new Game(
                null,
                "Details Test Game",
                "Test Publisher",
                "Adventure",
                new BigDecimal("19.99")
        ));

        mockMvc.perform(get("/api/v1/games/{gameId}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Details Test Game"))
                .andExpect(jsonPath("$.publisher").value("Test Publisher"))
                .andExpect(jsonPath("$.category").value("Adventure"))
                .andExpect(jsonPath("$.price").value(19.99));
    }
}