package CST438Project2.com.demo.repository;

import CST438Project2.com.demo.model.Game;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class GameRepository {

    private final JdbcTemplate jdbcTemplate;

    public GameRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Game save(Game game) {
        String sql = """
                INSERT INTO "GAME" ("name", "publisher", "category", "price")
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, game.getName());
            statement.setString(2, game.getPublisher());
            statement.setString(3, game.getCategory());
            statement.setBigDecimal(4, game.getPrice());
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("The database did not return an id for the new game");
        }

        return new Game(generatedId.longValue(), game.getName(), game.getPublisher(),
                game.getCategory(), game.getPrice());
    }

    public List<Game> findAll() {
        return jdbcTemplate.query("""
                        SELECT "id", "name", "publisher", "category", "price"
                        FROM "GAME"
                        ORDER BY "id"
                        """,
                (resultSet, rowNum) -> new Game(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("publisher"),
                        resultSet.getString("category"),
                        resultSet.getBigDecimal("price")));
    }

    /**
     * Retrieves one page of games in a consistent ID order.
     *
     * @param page zero-based page number
     * @param size maximum number of games per page
     * @return the games on the requested page
     */
    public List<Game> findPage(int page, int size) {
        long offset = (long) page * size;

        return jdbcTemplate.query("""
                    SELECT "id", "name", "publisher", "category", "price"
                    FROM "GAME"
                    ORDER BY "id"
                    LIMIT ? OFFSET ?
                    """,
                (resultSet, rowNum) -> new Game(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("publisher"),
                        resultSet.getString("category"),
                        resultSet.getBigDecimal("price")),
                size,
                offset);
    }

    /**
     * Retrieves one game from ID .
     *
     * @param id games id number
     * @return the game with the matching id number
     */

    public Optional<Game> findById(Long id) {
        return jdbcTemplate.query("""
            SELECT "id", "name", "publisher", "category", "price"
            FROM "GAME" WHERE "id" = ?
            """, (rs, row) -> new Game(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("publisher"),
                        rs.getString("category"),
                        rs.getBigDecimal("price")), id)
                .stream().findFirst();
    }

    public Optional<Game> update(
            Long id,
            String name,
            String publisher,
            String category,
            BigDecimal price) {

        Optional<Game> existing = findById(id);

        if(existing.isEmpty()){
            return Optional.empty();
        }

        Game game = existing.get();

        if(name != null){
            game.setName(name.trim());
        }

        if(publisher != null){
            game.setPublisher(publisher.trim());
        }

        if(category != null){
            game.setCategory(category.trim());
        }

        if(price != null){
            game.setPrice(price);
        }

        jdbcTemplate.update("""
            UPDATE "GAME"
            SET "name" = ?, "publisher" = ?, "category" = ?, "price" = ?
            WHERE "id" = ?
            """,
                game.getName(),
                game.getPublisher(),
                game.getCategory(),
                game.getPrice(),
                id);

        return Optional.of(game);
    }

    /**
     * Counts all games in the catalog.
     *
     * @return the total number of games
     */
    public long count() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"GAME\"",
                Long.class);
    }
}
