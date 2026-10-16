
package CST438Project2.com.demo.controller;

import CST438Project2.com.demo.model.Review;
import CST438Project2.com.demo.repository.ReviewRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void reviewCanBeCreated() throws Exception {
        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewJson("Great game!")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.comment").value("Great game!"))
                .andExpect(jsonPath("$.rating").value(4.5))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.gameId").value(1));
    }

    @Test
    void createdReviewCanBeRetrievedById() throws Exception {
        Review review = reviewRepository.save(new Review(
                null,
                LocalDate.of(2026, 10, 6),
                "Really fun game!",
                4.5f,
                2L,
                1L
        ));

        Long id = review.getId();

        mockMvc.perform(get("/api/v1/reviews/{reviewId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.comment").value("Really fun game!"))
                .andExpect(jsonPath("$.rating").value(4.5))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.gameId").value(1));
    }

    @Test
    void reviewsForGameCanBeViewedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/games/1/reviews"))
                .andExpect(status().isOk());
    }

    @Test
    void reviewCanBeViewedWithoutAuthentication() throws Exception {
        Review review = reviewRepository.save(new Review(
                null,
                LocalDate.of(2026, 10, 6),
                "Public review",
                4.0f,
                2L,
                1L
        ));

        mockMvc.perform(get("/api/v1/reviews/{reviewId}", review.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Public review"));
    }

    @Test
    void reviewsForGameAreReturned() throws Exception {
        Review review = reviewRepository.save(new Review(
                null,
                LocalDate.of(2026, 10, 6),
                "Game review test",
                5.0f,
                2L,
                1L
        ));

        mockMvc.perform(get("/api/v1/games/{gameId}/reviews", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$[?(@.id == " + review.getId() + ")]"
                ).isNotEmpty())
                .andExpect(jsonPath(
                        "$[?(@.comment == 'Game review test')]"
                ).isNotEmpty());
    }

    @Test
    void missingReviewReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void gameWithNoReviewsReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/games/999999999/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void savedReviewPersistsInDatabase() throws Exception {
        Review review = reviewRepository.save(new Review(
                null,
                LocalDate.of(2026, 10, 6),
                "Persistent review",
                3.5f,
                2L,
                1L
        ));

        Review saved = reviewRepository.findById(
                review.getId()
        ).orElseThrow();

        assertEquals("Persistent review", saved.getComment());
        assertEquals(3.5f, saved.getRating());
        assertEquals(2L, saved.getUserId());
        assertEquals(1L, saved.getGameId());
    }

    @Test
    void reviewsAreFilteredByGameId() throws Exception {
        Review review = reviewRepository.save(new Review(
                null,
                LocalDate.of(2026, 10, 6),
                "Filtered review",
                4.0f,
                2L,
                1L
        ));

        assertTrue(
                reviewRepository.findByGameId(1L)
                        .stream()
                        .anyMatch(r -> r.getId().equals(review.getId()))
        );
    }

    @Test
    void adminCanUpdateReview() throws Exception {
        Review review = reviewRepository.save(new Review(
                null,
                LocalDate.of(2026, 10, 6),
                "Before update",
                3.0f,
                2L,
                1L
        ));

        Long id = review.getId();

        mockMvc.perform(patch("/api/v1/reviews/{reviewId}", id)
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "comment": "After update",
                              "rating": 5.0
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("After update"))
                .andExpect(jsonPath("$.rating").value(5.0));

        Review updated = reviewRepository.findById(id).orElseThrow();

        assertEquals("After update", updated.getComment());
        assertEquals(5.0f, updated.getRating());
    }

    @Test
    void adminCanPatchOnlyComment() throws Exception {
        Review review = reviewRepository.save(new Review(
                null,
                LocalDate.of(2026, 10, 6),
                "Original comment",
                4.0f,
                2L,
                1L
        ));

        Long id = review.getId();

        mockMvc.perform(patch("/api/v1/reviews/{reviewId}", id)
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "comment": "Updated comment"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Updated comment"))
                .andExpect(jsonPath("$.rating").value(4.0));
    }

    @Test
    void adminCanPatchOnlyRating() throws Exception {
        Review review = reviewRepository.save(new Review(
                null,
                LocalDate.of(2026, 10, 6),
                "Keep comment",
                3.0f,
                2L,
                1L
        ));

        Long id = review.getId();

        mockMvc.perform(patch("/api/v1/reviews/{reviewId}", id)
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "rating": 4.5
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Keep comment"))
                .andExpect(jsonPath("$.rating").value(4.5));
    }

    @Test
    void unauthenticatedRequestsCannotPatchReviews() throws Exception {
        mockMvc.perform(patch("/api/v1/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"No\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotPatchReviews() throws Exception {
        mockMvc.perform(patch("/api/v1/reviews/1")
                        .with(httpBasic("user", "user-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"No\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingReviewCannotBeUpdated() throws Exception {
        mockMvc.perform(patch("/api/v1/reviews/999999999")
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"Missing\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidPatchReturnsBadRequest() throws Exception {
        Review review = reviewRepository.save(new Review(
                null,
                LocalDate.of(2026, 10, 6),
                "Valid review",
                4.0f,
                2L,
                1L
        ));

        Long id = review.getId();

        mockMvc.perform(patch("/api/v1/reviews/{reviewId}", id)
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "comment": "Invalid update",
                              "gameId": 999
                            }
                            """))
                .andExpect(status().isBadRequest());

        Review unchanged = reviewRepository.findById(id).orElseThrow();

        assertEquals("Valid review", unchanged.getComment());
    }

    private static String reviewJson(String comment) {
        return """
                {
                  "reviewDate": "2026-10-06",
                  "comment": "%s",
                  "rating": 4.5,
                  "userId": 2,
                  "gameId": 1
                }
                """.formatted(comment);
    }
}
