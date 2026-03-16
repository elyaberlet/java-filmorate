package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User createUser(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.debug("Пользователь добавлен в хранилище с id: {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с id {} не найден в хранилище", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        User existingUser = users.get(user.getId());

        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            user.setFriends(existingUser.getFriends());
        }

        users.put(user.getId(), user);
        log.debug("Пользователь с id {} обновлен в хранилище", user.getId());
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        log.debug("Запрошен список всех пользователей. Количество: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public User findUserById(Long id) {
        if (!users.containsKey(id)) {
            log.error("Пользователь с id {} не найден в хранилище", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        log.debug("Пользователь с id {} найден в хранилище", id);
        return users.get(id);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}