package chess.dao;

import chess.controller.dto.response.GameIdentifiers;
import chess.domain.GameState;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class GameDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert insertGame;

    public GameDao(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.insertGame = new SimpleJdbcInsert(dataSource)
                .withTableName("game");
    }

    public void save(Long id, String name, String password, String salt) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("game_id", id)
                .addValue("game_name", name)
                .addValue("game_password", password)
                .addValue("salt", salt)
                .addValue("state", GameState.READY);
        insertGame.execute(parameters);
    }

    public List<GameIdentifiers> findAllGames() {
        String sql = "SELECT game_id, game_name FROM game";
        return namedParameterJdbcTemplate.query(sql,
                (resultSet, rowNum) -> new GameIdentifiers(resultSet.getLong("game_id"),
                        resultSet.getString("game_name")));
    }

    public Optional<String> findName(Long id) {
        String sql = "SELECT game_name FROM game WHERE game_id = :game_id";
        return find(sql, id);
    }

    public Optional<String> findPassword(Long id) {
        String sql = "SELECT game_password FROM game WHERE game_id = :game_id";
        return find(sql, id);
    }

    public Optional<String> findSalt(Long id) {
        String sql = "SELECT salt FROM game WHERE game_id = :game_id";
        return find(sql, id);
    }

    private Optional<String> find(String sql, Long id) {
        SqlParameterSource namedParameters = new MapSqlParameterSource("game_id", id);
        try {
            return Optional.ofNullable(
                    namedParameterJdbcTemplate.queryForObject(sql, namedParameters, String.class));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<GameState> findState(Long id) {
        String sql = "SELECT state FROM game WHERE game_id = :game_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("game_id", id);
        try {
            return Optional.ofNullable(
                    namedParameterJdbcTemplate.queryForObject(sql, namedParameters, GameState.class));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void updateState(Long id, GameState gameState) {
        String sql = "UPDATE game SET state = :game_state WHERE game_id = :game_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("game_state", gameState.name())
                .addValue("game_id", id);
        namedParameterJdbcTemplate.update(sql, namedParameters);
    }

    public void delete(Long id) {
        String sql = "DELETE FROM game WHERE game_id = :game_id";
        MapSqlParameterSource parameter = new MapSqlParameterSource("game_id", id);
        namedParameterJdbcTemplate.update(sql, parameter);
    }
}
