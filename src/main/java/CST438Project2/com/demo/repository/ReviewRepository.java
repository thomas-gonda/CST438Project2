package CST438Project2.com.demo.repository;

import CST438Project2.com.demo.model.Review;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;

@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Review save(Review review) {
        String sql = """
            INSERT INTO "REVIEW"
                ("reviewDate", comment, rating, "userId", "gameId")
            VALUES (?, ?, ?, ?, ?)
            RETURNING id, "reviewDate", comment, rating, "userId", "gameId"
            """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new Review(
                        rs.getLong("id"),
                        rs.getDate("reviewDate").toLocalDate(),
                        rs.getString("comment"),
                        rs.getFloat("rating"),
                        rs.getLong("userId"),
                        rs.getLong("gameId")
                ),
                Date.valueOf(review.getReviewDate()),
                review.getComment(),
                review.getRating(),
                review.getUserId(),
                review.getGameId()
        );
    }
}
