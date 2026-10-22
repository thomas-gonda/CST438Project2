package CST438Project2.com.demo.controller;

import CST438Project2.com.demo.model.Game;
import CST438Project2.com.demo.model.Review;
import CST438Project2.com.demo.model.ReviewPage;
import CST438Project2.com.demo.model.User;
import CST438Project2.com.demo.repository.GameRepository;
import CST438Project2.com.demo.repository.ReviewRepository;
import CST438Project2.com.demo.repository.UserRepository;
import CST438Project2.com.demo.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Handles game review requests.
 */
@RestController
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService;

    /**
     * Creates the controller with its repositories and service.
     *
     * @param reviewRepository repository used to save and find reviews
     * @param gameRepository repository used to verify games
     * @param userRepository repository used to resolve authenticated users
     * @param reviewService service used for review pagination
     */
    public ReviewController(ReviewRepository reviewRepository,
            GameRepository gameRepository, UserRepository userRepository,
            ReviewService reviewService) {
        this.reviewRepository = reviewRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.reviewService = reviewService;
    }

    /**
     * Creates a review for a game using the authenticated database user.
     *
     * @param gameId game being reviewed
     * @param request comment and rating supplied by the client
     * @param authentication authenticated principal used to resolve ownership
     * @return the created review
     */
    @PostMapping("/api/v1/games/{gameId}/reviews")
    public ResponseEntity<?> createReviewForGame(@PathVariable Long gameId,
            @Valid @RequestBody ReviewCreateRequest request,
            Authentication authentication) {
        Optional<Game> game = gameRepository.findById(gameId);
        if (game.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<User> user = authenticatedUser(authentication);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Review review = new Review(null, LocalDate.now(),
                request.comment().trim(), request.rating(), user.get().getId(), gameId);
        Review createdReview = reviewRepository.save(review);
        return ResponseEntity.created(
                URI.create("/api/v1/reviews/" + createdReview.getId()))
                .body(createdReview);
    }

    /**
     * Lists reviews for a game with pagination.
     *
     * @param gameId game whose reviews should be returned
     * @param page zero-based page number
     * @param size number of reviews per page
     * @return a paginated review response
     */
    @GetMapping("/api/v1/games/{gameId}/reviews")
    public ResponseEntity<ReviewPage> getReviewsByGameId(@PathVariable Long gameId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        if (gameRepository.findById(gameId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reviewService.listByGameId(gameId, page, size));
    }

    /**
     * Lists reviews belonging to the authenticated database user.
     *
     * @param page zero-based page number
     * @param size number of reviews per page
     * @param authentication authenticated principal
     * @return a paginated review response
     */
    @GetMapping("/api/v1/users/me/reviews")
    public ResponseEntity<ReviewPage> getMyReviews(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            Authentication authentication) {
        Optional<User> user = authenticatedUser(authentication);
        return user.map(value -> ResponseEntity.ok(reviewService.listByUserId(value.getId(), page, size))).orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    /**
     * Retrieves one review by id.
     *
     * @param reviewId review to retrieve
     * @return the review when it exists
     */
    @GetMapping("/api/v1/reviews/{reviewId}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long reviewId) {
        return reviewRepository.findById(reviewId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a review. Admin authorization remains the temporary local
     * ownership boundary until OAuth2 identity mapping is integrated.
     *
     * @param reviewId review to delete
     * @return no content when deleted, or not found
     */
    @DeleteMapping("/api/v1/reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        if (reviewRepository.findById(reviewId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        reviewRepository.deleteById(reviewId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Fully replaces the editable fields of a review.
     *
     * @param reviewId review to update
     * @param request replacement comment and rating
     * @return the updated review, or not found
     */
    @PutMapping("/api/v1/reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Review> replaceReview(@PathVariable Long reviewId,
            @Valid @RequestBody ReviewReplaceRequest request) {
        Optional<Review> existing = reviewRepository.findById(reviewId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Review review = existing.get();
        review.setComment(request.comment().trim());
        review.setRating(request.rating());
        reviewRepository.update(review);
        return ResponseEntity.ok(review);
    }

    /**
     * Updates one or more editable fields of a review.
     *
     * @param reviewId review to update
     * @param updates fields to change
     * @return the updated review, or an error response
     */
    @PatchMapping("/api/v1/reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Review> updateReview(@PathVariable Long reviewId,
            @RequestBody Map<String, Object> updates) {
        Optional<Review> existing = reviewRepository.findById(reviewId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (updates == null || updates.isEmpty() || updates.keySet().stream()
                .anyMatch(key -> !key.equals("comment") && !key.equals("rating"))) {
            return ResponseEntity.badRequest().build();
        }

        Review review = existing.get();
        if (updates.containsKey("comment")) {
            Object comment = updates.get("comment");
            if (!(comment instanceof String) || ((String) comment).isBlank()
                    || ((String) comment).length() > 1000) {
                return ResponseEntity.badRequest().build();
            }
            review.setComment((String) comment);
        }
        if (updates.containsKey("rating")) {
            Object rating = updates.get("rating");
            if (!(rating instanceof Number)) {
                return ResponseEntity.badRequest().build();
            }
            float value = ((Number) rating).floatValue();
            if (!Float.isFinite(value)) {
                return ResponseEntity.badRequest().build();
            }
            review.setRating(value);
        }
        reviewRepository.update(review);
        return ResponseEntity.ok(review);
    }

    private Optional<User> authenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String identity = authentication.getName();
        String displayName = identity;

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            identity = firstNonBlank(
                    jwt.getClaimAsString("email"),
                    jwt.getSubject(),
                    identity
            );
            displayName = firstNonBlank(
                    jwt.getClaimAsString("name"),
                    jwt.getClaimAsString("nickname"),
                    jwt.getClaimAsString("preferred_username"),
                    identity
            );
        }

        if (identity == null || identity.isBlank()) {
            return Optional.empty();
        }

        String databaseIdentity = identity.trim();
        Optional<User> existing = userRepository.findByEmail(databaseIdentity);
        if (existing.isPresent()) {
            return existing;
        }

        // Auth0 users do not necessarily exist in the application's USER table.
        // Provision the authenticated user on first use so a valid bearer token
        // is not turned into a second, misleading 401 response.
        User provisioned = new User(
                null,
                displayName == null || displayName.isBlank()
                        ? databaseIdentity
                        : displayName.trim(),
                databaseIdentity,
                "USER"
        );
        return Optional.of(userRepository.save(provisioned));
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    /**
     * Converts invalid pagination arguments into a client error.
     *
     * @param exception invalid argument exception
     * @return bad request response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleInvalidArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().build();
    }

    private record ReviewCreateRequest(@NotBlank @Size(max = 1000) String comment,
            @NotNull Float rating) {
    }

    private record ReviewReplaceRequest(@NotBlank @Size(max = 1000) String comment,
            @NotNull Float rating) {
    }
}
