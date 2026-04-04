package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User createUser(User user) {
        setNameIfBlank(user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        if (user.getId() == null || user.getId() <= 0) {
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }

        User existing = userStorage.findUserById(user.getId())
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id=" + user.getId() + " не найден"));

        setNameIfBlank(user);
        return userStorage.updateUser(user);
    }


    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User findUserById(Long id) {
        return userStorage.findUserById(id)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id=" + id + " не найден"));
    }


    public void setNameIfBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя не указано, будет использован логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}