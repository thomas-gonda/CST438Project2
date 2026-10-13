
package CST438Project2.com.demo.controller;

import CST438Project2.com.demo.model.Review;
import CST438Project2.com.demo.repository.ReviewRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class ReviewController {

    private final ReviewRepository reviewRepository;

    public ReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @PostMapping("/reviews")
    public ResponseEntity<Review> createReview(@RequestBody Review review) {

        Review createdReview = reviewRepository.save(review);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdReview);
    }

    @GetMapping("/api/v1/games/{gameId}/reviews")
    public ResponseEntity<List<Review>> getReviewsByGameId(
            @PathVariable Long gameId) {

        List<Review> reviews = reviewRepository.findByGameId(gameId);

        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/api/v1/reviews/{reviewId}")
    public ResponseEntity<Review> getReviewById(
            @PathVariable Long reviewId) {

        Optional<Review> review = reviewRepository.findById(reviewId);

        if (review.isPresent()) {
            return ResponseEntity.ok(review.get());
        }

        return ResponseEntity.notFound().build();
    }
}
