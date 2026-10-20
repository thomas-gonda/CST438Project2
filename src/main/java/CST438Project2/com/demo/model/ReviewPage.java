package CST438Project2.com.demo.model;

import java.util.List;

/**
 * A page of reviews with pagination information.
 *
 * @param content reviews on this page
 * @param page pagination information
 */
public record ReviewPage(List<Review> content, PageMeta page) {

    /**
     * Describes the requested page and the complete result set.
     *
     * @param size maximum reviews per page
     * @param number zero-based page number
     * @param totalElements total number of reviews
     * @param totalPages total number of pages
     */
    public record PageMeta(
            int size,
            int number,
            long totalElements,
            long totalPages) {
    }
}
