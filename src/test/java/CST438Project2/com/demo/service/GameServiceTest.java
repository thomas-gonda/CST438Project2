package CST438Project2.com.demo.service;

import CST438Project2.com.demo.model.Game;
import CST438Project2.com.demo.model.GamePage;
import CST438Project2.com.demo.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameServiceTest {

    private GameRepository repository;
    private GameService service;

    @BeforeEach
    void setUp() {
        repository = mock(GameRepository.class);
        service = new GameService(repository);
    }

    @Test
    void returnsRequestedPageAndRoundsUpTotalPages() {
        Game game = new Game(
                21L, "Example Game", "Publisher",
                "Adventure", new BigDecimal("19.99"));

        when(repository.count()).thenReturn(21L);
        when(repository.findPage(1, 20)).thenReturn(List.of(game));

        GamePage result = service.listGames(1, 20);

        assertEquals(List.of(game), result.content());
        assertEquals(1, result.page().number());
        assertEquals(20, result.page().size());
        assertEquals(21L, result.page().totalElements());
        assertEquals(2L, result.page().totalPages());
        verify(repository).findPage(1, 20);
    }

    @Test
    void returnsEmptyPageWhenNoGamesExist() {
        when(repository.count()).thenReturn(0L);
        when(repository.findPage(0, 20)).thenReturn(List.of());

        GamePage result = service.listGames(0, 20);

        assertTrue(result.content().isEmpty());
        assertEquals(0L, result.page().totalElements());
        assertEquals(0L, result.page().totalPages());
    }

    @Test
    void exactPageSizeDoesNotCreateAnExtraPage() {
        when(repository.count()).thenReturn(40L);
        when(repository.findPage(0, 20)).thenReturn(List.of());

        GamePage result = service.listGames(0, 20);

        assertEquals(2L, result.page().totalPages());
    }

    @Test
    void pageBeyondAvailableGamesReturnsEmptyContent() {
        when(repository.count()).thenReturn(1L);
        when(repository.findPage(5, 20)).thenReturn(List.of());

        GamePage result = service.listGames(5, 20);

        assertTrue(result.content().isEmpty());
        assertEquals(5, result.page().number());
        assertEquals(1L, result.page().totalElements());
        assertEquals(1L, result.page().totalPages());
    }

    @Test
    void rejectsNegativePageBeforeQueryingDatabase() {
        assertThrows(IllegalArgumentException.class,
                () -> service.listGames(-1, 20));

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsInvalidSizesBeforeQueryingDatabase() {
        for (int size : new int[]{-1, 0, 101}) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.listGames(0, size));
        }

        verifyNoInteractions(repository);
    }
}