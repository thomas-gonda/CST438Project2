package CST438Project2.com.demo.controller;

import CST438Project2.com.demo.model.Game;
import CST438Project2.com.demo.repository.GameRepository;
import CST438Project2.com.demo.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GameListControllerTest {

    private GameRepository repository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        repository = mock(GameRepository.class);
        GameService service = new GameService(repository);
        GameController controller = new GameController(repository, service);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void returnsGameFieldsAndDefaultPagination() throws Exception {
        Game game = new Game(
                1L, "Example Game", "Example Publisher",
                "Adventure", new BigDecimal("19.99"));

        when(repository.count()).thenReturn(1L);
        when(repository.findPage(0, 20)).thenReturn(List.of(game));

        mockMvc.perform(get("/api/v1/games"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Example Game"))
                .andExpect(jsonPath("$.content[0].publisher")
                        .value("Example Publisher"))
                .andExpect(jsonPath("$.content[0].category").value("Adventure"))
                .andExpect(jsonPath("$.content[0].price").value(19.99))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }

    @Test
    void returnsEmptyCollectionWhenNoGamesExist() throws Exception {
        when(repository.count()).thenReturn(0L);
        when(repository.findPage(0, 20)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0));
    }

    @Test
    void usesRequestedPageAndSize() throws Exception {
        Game game = new Game(
                2L, "Second Game", "Publisher",
                "Puzzle", new BigDecimal("9.99"));

        when(repository.count()).thenReturn(2L);
        when(repository.findPage(1, 1)).thenReturn(List.of(game));

        mockMvc.perform(get("/api/v1/games")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.size").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(2));

        verify(repository).findPage(1, 1);
    }

    @Test
    void rejectsInvalidPaginationRanges() throws Exception {
        String[][] invalidParameters = {
                {"page", "-1"},
                {"size", "0"},
                {"size", "101"}
        };

        for (String[] parameter : invalidParameters) {
            mockMvc.perform(get("/api/v1/games")
                            .param(parameter[0], parameter[1]))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.title").value("Bad Request"));
        }

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsNonIntegerPaginationValues() throws Exception {
        for (String parameter : new String[]{"page", "size"}) {
            mockMvc.perform(get("/api/v1/games")
                            .param(parameter, "abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(400));
        }

        verifyNoInteractions(repository);
    }
}