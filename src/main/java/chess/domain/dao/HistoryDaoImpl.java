package chess.domain.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class HistoryDaoImpl implements HistoryDao {
    private final JdbcTemplate jdbcTemplate;

    public HistoryDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<String> rowMapper = (resultSet, rowNum) -> resultSet.getString("name");

    @Override
    public void insert(String name) {
        final String query = "INSERT INTO History (name) VALUES (?)";
        jdbcTemplate.update(query, name);
    }
    @Override
    public Optional<Integer> findIdByName(String name) {
        final String query = "SELECT history_id FROM History WHERE name = ? AND is_end = false";
        return Optional.ofNullable(jdbcTemplate.queryForObject(query, Integer.class, name));
    }

    @Override
    public int delete(String name) {
        final String query = "DELETE FROM History WHERE Name = ?";
        return jdbcTemplate.update(query, name);
    }

    @Override
    public List<String> selectActive() {
        final String query = "SELECT * FROM History WHERE is_end = false";
        return jdbcTemplate.query(query, rowMapper);
    }

    @Override
    public void updateEndState(String id) {
        final String query = "UPDATE History SET is_end = 1 WHERE history_id = ?";
        jdbcTemplate.update(query, id);
    }
}