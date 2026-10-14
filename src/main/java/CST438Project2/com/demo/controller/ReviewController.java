
package CST438Project2.com.demo.controller;

import CST438Project2.com.demo.model.Review;
import CST438Project2.com.demo.repository.ReviewRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

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

    // updates one or more fields of an existing review
    @PatchMapping("/api/v1/reviews/{reviewId}")
    public ResponseEntity<Review> updateReview(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> updates) {

        Optional<Review> existing = reviewRepository.findById(reviewId);

        if(existing.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Review review = existing.get();

        if(updates == null || updates.isEmpty() || updates.keySet().stream().anyMatch(
                key -> !key.equals("comment") && !key.equals("rating"))){
            return ResponseEntity.badRequest().build();
        }

        if(updates.containsKey("comment")){
            Object comment = updates.get("comment");

            if(!(comment instanceof String) || ((String) comment).length() > 1000){
                return ResponseEntity.badRequest().build();
            }

            review.setComment((String) comment);
        }

        if(updates.containsKey("rating")){
            Object rating = updates.get("rating");

            if(!(rating instanceof Number)){
                return ResponseEntity.badRequest().build();
            }

            float value = ((Number) rating).floatValue();

            if(!Float.isFinite(value)){
                return ResponseEntity.badRequest().build();
            }

            review.setRating(value);
        }

        reviewRepository.update(review);

        return ResponseEntity.ok(review);
    }
}
