package CST438Project2.com.demo.controller;

import CST438Project2.com.demo.model.Game;
import CST438Project2.com.demo.repository.GameRepository;
import CST438Project2.com.demo.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GameDetailsControllerTest {

    private GameRepository repository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        repository = mock(GameRepository.class);
        GameService service = new GameService(repository);

        mockMvc = MockMvcBuilders.standaloneSetup(
                new GameController(repository, service)).build();
    }

    @Test
    void databaseFailureReturnsProblemDetails() throws Exception {
        when(repository.findById(7L))
                .thenThrow(new DataAccessResourceFailureException(
                        "Database connection failed"));

        mockMvc.perform(get("/api/v1/games/7"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail")
                        .value("An unexpected database error occurred."));
    }

    @Test
    void returnsRequestedGameWithAllFields() throws Exception {
        Game game = new Game(
                7L, "Example Game", "Example Publisher",
                "Adventure", new BigDecimal("19.99"));

        when(repository.findById(7L)).thenReturn(Optional.of(game));

        mockMvc.perform(get("/api/v1/games/7"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Example Game"))
                .andExpect(jsonPath("$.publisher").value("Example Publisher"))
                .andExpect(jsonPath("$.category").value("Adventure"))
                .andExpect(jsonPath("$.price").value(19.99));

        verify(repository).findById(7L);
    }

    @Test
    void missingGameReturnsProblemDetails() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/games/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail")
                        .value("Game 99 was not found."));

        verify(repository).findById(99L);
    }

    @Test
    void invalidGameIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/games/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"));

        verifyNoInteractions(repository);
    }
}