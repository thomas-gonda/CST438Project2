package CST438Project2.com.demo.controller;

import CST438Project2.com.demo.model.Review;
import CST438Project2.com.demo.repository.ReviewRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;

    public ReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {

        Review createdReview = reviewRepository.save(review);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdReview);
    }
}
