package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private static final String FIND_BY_ID_SQL = "SELECT * FROM users WHERE user_id = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM users";
    private static final String INSERT_SQL = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
    private static final String DELETE_SQL = "DELETE FROM users WHERE user_id = ?";

    public UserDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public User createUser(User user) {
        long id = insert(
                INSERT_SQL,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public User updateUser(User user) {
        update(UPDATE_SQL,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
                );
        return user;
    }

    @Override
    public Collection<User> getAllUsers() {
        return findMany(FIND_ALL_SQL);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return findOne(FIND_BY_ID_SQL, id);
    }

    @Override
    public boolean deleteUser(long id) {
        return delete(DELETE_SQL, id);
    }
}
