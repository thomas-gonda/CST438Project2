package CST438Project2.com.demo.service;

import CST438Project2.com.demo.model.Game;
import CST438Project2.com.demo.model.GamePage;
import CST438Project2.com.demo.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles game catalog retrieval and pagination.
 */
@Service
public class GameService {

    private final GameRepository gameRepository;

    /**
     * Creates the service with its database repository.
     *
     * @param gameRepository repository used to retrieve games
     */
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * Retrieves a page of games and pagination information.
     *
     * @param page zero-based page number
     * @param size number of games per page, from 1 to 100
     * @return games and pagination information
     * @throws IllegalArgumentException if page or size is invalid
     */
    public GamePage listGames(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "page must be zero or greater");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "size must be between 1 and 100");
        }

        long totalElements = gameRepository.count();
        List<Game> games = gameRepository.findPage(page, size);

        long totalPages = totalElements / size;
        if (totalElements % size != 0) {
            totalPages++;
        }

        return new GamePage(
                games,
                new GamePage.PageMeta(
                        size, page, totalElements, totalPages));
    }
}