package CST438Project2.com.demo.model;

import java.util.List;

/**
 * A page of games with pagination information.
 *
 * @param content games on this page
 * @param page pagination information
 */
public record GamePage(List<Game> content, PageMeta page) {

    /**
     * Describes the requested page and the complete result set.
     *
     * @param size maximum games per page
     * @param number zero-based page number
     * @param totalElements total number of games
     * @param totalPages total number of pages
     */
    public record PageMeta(
            int size,
            int number,
            long totalElements,
            long totalPages) {
    }
}