package CST438Project2.com.demo.controller;

import CST438Project2.com.demo.model.Game;
import CST438Project2.com.demo.model.GamePage;
import CST438Project2.com.demo.repository.GameRepository;
import CST438Project2.com.demo.service.GameService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.net.URI;

/**
 * Handles game catalog requests.
 */
@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameRepository gameRepository;
    private final GameService gameService;

    /**
     * Creates the controller with its repository and service.
     *
     * @param gameRepository repository used to save games
     * @param gameService service used to retrieve game pages
     */
    public GameController(
            GameRepository gameRepository,
            GameService gameService) {
        this.gameRepository = gameRepository;
        this.gameService = gameService;
    }

    /**
     * Returns a page of publicly available games.
     *
     * @param page zero-based page number, defaults to 0
     * @param size games per page, defaults to 20
     * @return games and pagination information
     */
    @GetMapping
    @Operation(summary = "List games")
    @ApiResponse(responseCode = "200", description = "Games returned")
    public GamePage listGames(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return gameService.listGames(page, size);
    }

    /**
     * Returns the details of one game.
     *
     * @param gameId the game to retrieve
     * @return the game when found, or a problem response when missing
     */
    @GetMapping("/{gameId}")
    @Operation(summary = "Get a game by id")
    @ApiResponse(responseCode = "200", description = "Game returned")
    public ResponseEntity<?> getGameById(
            @PathVariable("gameId") Long gameId) {

        return gameRepository.findById(gameId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ProblemDetail.forStatusAndDetail(
                                HttpStatus.NOT_FOUND,
                                "Game " + gameId + " was not found.")));
    }

    /**
     * Creates a game for an administrator.
     *
     * @param request the validated game details
     * @return the created game and its location
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a game")
    @ApiResponse(responseCode = "201", description = "Game created")
    public ResponseEntity<Game> addGame(@Valid @RequestBody GameRequest request) {
        Game game = gameRepository.save(new Game(
                null,
                request.name().trim(),
                request.publisher().trim(),
                request.category().trim(),
                request.price()));

        return ResponseEntity.created(
                URI.create("/api/v1/games/" + game.getId())).body(game);
    }

    /**
     * Updates a game for an administrator.
     *
     * @param request the validated game details
     * @return the updated game
     */
    @PatchMapping("/{gameId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a game")
    @ApiResponse(responseCode = "200", description = "Game updated")
    public ResponseEntity<Game> updateGame(
            @PathVariable Long gameId,
            @RequestBody GameUpdateRequest request) {

        if(request == null){
            return ResponseEntity.badRequest().build();
        }

        if((request.name() != null && request.name().isBlank())
                || (request.publisher() != null && request.publisher().isBlank())
                || (request.category() != null && request.category().isBlank())
                || (request.price() != null && request.price().signum() < 0)){
            return ResponseEntity.badRequest().build();
        }

        return gameRepository.update(
                        gameId,
                        request.name(),
                        request.publisher(),
                        request.category(),
                        request.price())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a game for an administrator.
     *
     * @param gameId the game to delete
     * @return no content when deleted, or a problem response when it is missing
     */
    @DeleteMapping("/{gameId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a game")
    @ApiResponse(responseCode = "204", description = "Game deleted")
    public ResponseEntity<?> deleteGame(@PathVariable Long gameId) {
        if (gameRepository.deleteById(gameId)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        "Game " + gameId + " was not found."));
    }

    private record GameUpdateRequest(
            String name,
            String publisher,
            String category,
            BigDecimal price) {
    }

    /**
     * Returns a problem response when a database operation fails.
     */
    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDatabaseError(DataAccessException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected database error occurred.");
    }

    /**
     * Handles invalid pagination ranges.
     *
     * @param exception the validation failure
     * @return a problem response with status 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidArgument(
            IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    /**
     * Handles query parameters that cannot be converted to their required type.
     *
     * @param exception the parameter conversion failure
     * @return a problem response with status 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleInvalidParameterType(
            MethodArgumentTypeMismatchException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid value for parameter '" + exception.getName()
                        + "': expected a whole number.");
    }

    private record GameRequest(
            @NotBlank(message = "name is required") String name,
            @NotBlank(message = "publisher is required") String publisher,
            @NotBlank(message = "category is required") String category,
            @NotNull(message = "price is required")
            @DecimalMin(value = "0.00", message = "price must be zero or greater")
            BigDecimal price) {
    }
}
