package CST438Project2.com.demo.model;

import java.time.LocalDate;

public class Review {

    private Long id;
    private LocalDate reviewDate;
    private String comment;
    private Float rating;
    private Long userId;
    private Long gameId;

    public Review() {
    }

    public Review(
            Long id,
            LocalDate reviewDate,
            String comment,
            Float rating,
            Long userId,
            Long gameId
    ) {
        this.id = id;
        this.reviewDate = reviewDate;
        this.comment = comment;
        this.rating = rating;
        this.userId = userId;
        this.gameId = gameId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", reviewDate=" + reviewDate +
                ", comment='" + comment + '\'' +
                ", rating=" + rating +
                ", userId=" + userId +
                ", gameId=" + gameId +
                '}';
    }
}
