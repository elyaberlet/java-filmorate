package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Repository
public class FriendshipDbStorage extends BaseDbStorage<User> {

    private static final String CHECK_FRIENDSHIP_SQL =
            "SELECT status FROM friendship WHERE user_id = ? AND friend_id = ?";

    private static final String GET_FRIENDS_SQL =
            "SELECT u.* FROM users u " +
                    "JOIN friendship f ON u.user_id = f.friend_id " +
                    "WHERE f.user_id = ?";

    private static final String GET_COMMON_FRIENDS_SQL =
            "SELECT u.* FROM users u " +
                    "WHERE u.user_id IN (" +
                    "    SELECT f1.friend_id FROM friendship f1 WHERE f1.user_id = ?" +
                    ") " +
                    "AND u.user_id IN (" +
                    "    SELECT f2.friend_id FROM friendship f2 WHERE f2.user_id = ?" +
                    ")";

    private static final String ADD_FRIEND_SQL =
            "INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, 'confirmed')";

    private static final String DELETE_FRIEND_SQL =
            "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

    private static final String CHECK_FRIENDSHIP_EXISTS =
            "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?";

    public FriendshipDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    public void addFriend(long userId, long friendId) {
        Integer count = jdbc.queryForObject(CHECK_FRIENDSHIP_EXISTS, Integer.class, userId, friendId);
        if (count == null || count == 0) {
            update(ADD_FRIEND_SQL, userId, friendId);
        }
    }

    public void removeFriend(long userId, long friendId) {
        update(DELETE_FRIEND_SQL, userId, friendId);
    }

    public List<User> getFriends(long userId) {
        return findMany(GET_FRIENDS_SQL, userId);
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        return findMany(GET_COMMON_FRIENDS_SQL, userId, otherId);
    }
    public String getFriendshipStatus(long userId, long friendId) {
        List<String> result = jdbc.query(
                CHECK_FRIENDSHIP_SQL,
                (rs, rowNum) -> rs.getString("status"),
                userId, friendId
        );
        return result.isEmpty() ? null : result.get(0);
    }

    public boolean isFriend(long userId, long friendId) {
        Integer count = jdbc.queryForObject(CHECK_FRIENDSHIP_EXISTS, Integer.class, userId, friendId);
        return count != null && count > 0;
    }
}