package chess.dao;

import chess.domain.entity.Room;
import chess.exception.InitialSettingDataException;
import chess.exception.NotEnoughPlayerException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Repository
public class RoomDAO {
    private static final String NO_USER = "no user";
    private static final int PLAYING_STATUS = 1;
    private static final int READY_STATUS = 2;

    private final JdbcTemplate jdbcTemplate;

    public RoomDAO(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createRoom(final String name, final int whiteUserId) {
        String query = "INSERT INTO room (title, white_user, status) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(query, new String[]{"id"});
            ps.setString(1, name);
            ps.setInt(2, whiteUserId);
            ps.setInt(3, READY_STATUS);
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public List<Room> allRooms() {
        String query = "SELECT * FROM room ORDER BY room.status DESC, room.id DESC";
        return jdbcTemplate.query(query, mapper());
    }

    private RowMapper<Room> mapper() {
        return (resultSet, rowNum) -> new Room(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getInt("black_user"),
                resultSet.getInt("white_user"),
                resultSet.getInt("status")
        );
    }

    public void changeStatusEndByRoomId(final String roomId) {
        String query = "UPDATE room SET status = 0 WHERE id = ?";
        jdbcTemplate.update(query, roomId);
    }

    public List<String> allRoomIds() {
        try {
            RowMapper<String> rowMapper = (resultSet, rowNum) -> resultSet.getString("id");

            String query = "SELECT id FROM room";
            return jdbcTemplate.query(query, rowMapper);
        } catch (DataAccessException e) {
            throw new InitialSettingDataException();
        }
    }

    public void joinBlackUser(final String roomId, final int blackUserId) {
        String query = "UPDATE room SET black_user = ?, status = ? WHERE id = ?";
        jdbcTemplate.update(query, blackUserId, PLAYING_STATUS, roomId);
    }

    public String findBlackUserById(final String id) {
        try {
            return jdbcTemplate.queryForObject("SELECT black_user FROM room WHERE id = ?",
                    String.class,
                    id);
        } catch (DataAccessException e) {
            return NO_USER;
        }
    }

    public String findWhiteUserById(final String id) {
        try {
            return jdbcTemplate.queryForObject("SELECT white_user FROM room WHERE id = ?",
                    String.class,
                    id);
        } catch (DataAccessException e) {
            return NO_USER;
        }
    }

    public String findUserIdByRoomIdAndColor(final String id, final String color) {
        try {
            String query = "SELECT " + color + "_user FROM room WHERE id = ?";
            return jdbcTemplate.queryForObject(query, String.class, id);
        } catch (DataAccessException e) {
            return NO_USER;
        }
    }

    public Room findPlayersByRoomId(final String roomId) {
        try {
            String query = "SELECT black_user, white_user FROM room WHERE room.id = ?";
            return jdbcTemplate.queryForObject(query, (resultSet, rowNum) -> new Room(
                    resultSet.getInt("black_user"),
                    resultSet.getInt("white_user")
            ), roomId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotEnoughPlayerException(roomId);
        }
    }
}
