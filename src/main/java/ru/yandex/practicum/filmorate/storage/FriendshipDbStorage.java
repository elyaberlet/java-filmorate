package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendshipDbStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    private static final String UPDATE_STATUS = "UPDATE friendship SET status_id = ? WHERE user_id = ? AND friend_id = ?";
    private static final String DELETE_FRIENDSHIP_SQL = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String GET_FRIENDS_SQL =
            "SELECT u.* FROM users u " +
                    "JOIN friendship f ON u.user_id = f.friend_id " +
                    "WHERE f.user_id = ?";

    private static final String GET_COMMON_FRIENDS_SQL =
            "SELECT u.* " +
                    "FROM users u " +
                    "WHERE u.user_id IN ( " +
                    "    SELECT f1.friend_id " +
                    "    FROM friendship f1 " +
                    "    WHERE f1.user_id = ? " +
                    " " +
                    "    INTERSECT " +
                    " " +
                    "    SELECT f2.friend_id " +
                    "    FROM friendship f2 " +
                    "    WHERE f2.user_id = ? " +
                    ") ";

    private static final String INSERT_FRIENDSHIP = "INSERT INTO friendship (user_id, friend_id, status_id) VALUES (?, ?, ?)";

    private static final String CHECK_FRIENDSHIP = "SELECT status_id " +
            "FROM friendship " +
            "WHERE user_id = ? AND friend_id = ?";

    public Integer getFriendshipStatus(long userId, long friendId) {
        List<Integer> result = jdbc.query(CHECK_FRIENDSHIP,
                (rs, rowNum) -> rs.getInt("status_id"), userId, friendId);

        return result.isEmpty() ? null : result.getFirst();
    }

    public void addFriend(long userId, long friendId) {
        Integer reverseStatus = getFriendshipStatus(friendId, userId);

        if (reverseStatus == null) {
            jdbc.update(INSERT_FRIENDSHIP, userId, friendId, 1);
        } else if (reverseStatus == 1) {
            jdbc.update(INSERT_FRIENDSHIP, userId, friendId, 2);
            jdbc.update(UPDATE_STATUS, 2, friendId, userId);
        }
    }

    public void removeFriend(long userId, long friendId) {

        jdbc.update(DELETE_FRIENDSHIP_SQL, userId, friendId);

        Integer reverseStatus = getFriendshipStatus(friendId, userId);

        if (reverseStatus != null && reverseStatus == 2) {
            jdbc.update(UPDATE_STATUS, 1, friendId, userId);
        }
    }

    public List<User> getFriends(long userId) {
        return jdbc.query(GET_FRIENDS_SQL, mapper, userId);
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        return jdbc.query(GET_COMMON_FRIENDS_SQL, mapper, userId, otherId);
    }
}
