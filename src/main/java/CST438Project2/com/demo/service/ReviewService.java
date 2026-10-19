package CST438Project2.com.demo.service;

import CST438Project2.com.demo.model.Review;
import CST438Project2.com.demo.model.ReviewPage;
import CST438Project2.com.demo.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles review retrieval and pagination.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    /**
     * Creates the service with its review repository.
     *
     * @param reviewRepository repository used to retrieve reviews
     */
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    /**
     * Lists reviews for one game.
     *
     * @param gameId game whose reviews should be returned
     * @param page zero-based page number
     * @param size number of reviews per page
     * @return a page of reviews
     * @throws IllegalArgumentException when page or size is invalid
     */
    public ReviewPage listByGameId(Long gameId, int page, int size) {
        validatePage(page, size);
        return listPage(page, size, reviewRepository.countByGameId(gameId),
                reviewRepository.findPageByGameId(gameId, page, size));
    }

    /**
     * Lists reviews owned by one database user.
     *
     * @param userId authenticated database user id
     * @param page zero-based page number
     * @param size number of reviews per page
     * @return a page of reviews
     * @throws IllegalArgumentException when page or size is invalid
     */
    public ReviewPage listByUserId(Long userId, int page, int size) {
        validatePage(page, size);
        return listPage(page, size, reviewRepository.countByUserId(userId),
                reviewRepository.findPageByUserId(userId, page, size));
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "page must be zero or greater");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "size must be between 1 and 100");
        }
    }

    private ReviewPage listPage(
            int page, int size, long totalElements, List<Review> reviews) {
        long totalPages = totalElements / size;
        if (totalElements % size != 0) {
            totalPages++;
        }

        return new ReviewPage(
                reviews,
                new ReviewPage.PageMeta(
                        size, page, totalElements, totalPages));
    }
}
