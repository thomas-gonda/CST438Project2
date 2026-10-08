package CST438Project2.com.demo.controller;

import CST438Project2.com.demo.model.Game;
import CST438Project2.com.demo.repository.GameRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameRepository gameRepository;

    public GameController(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @GetMapping
    public List<Game> listGames() {
        return gameRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Game> addGame(@Valid @RequestBody GameRequest request) {
        Game game = gameRepository.save(new Game(
                null,
                request.name().trim(),
                request.publisher().trim(),
                request.category().trim(),
                request.price()));

        return ResponseEntity.created(URI.create("/api/v1/games/" + game.getId())).body(game);
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
