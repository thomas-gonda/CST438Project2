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
}
