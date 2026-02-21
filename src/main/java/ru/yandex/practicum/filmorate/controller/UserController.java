package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAllUsers() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Создание пользователя с логином: {}", user.getLogin());
        validateLogin(user.getLogin());
        setNameIfBlank(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан с id: {}", user.getId());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Обновление пользователя с id: {}", user.getId());

        if (user.getId() == null) {
            log.warn("Попытка обновления без указания ID");
            throw new ValidationException("ID не может быть null");
        }

        if (!users.containsKey(user.getId())) {
            log.warn("Попытка обновления несуществующего пользователя с id: {}", user.getId());
            throw new ValidationException("Пользователь с id " + user.getId() + " не найден");
        }

        validateLogin(user.getLogin());
        setNameIfBlank(user);
        users.put(user.getId(), user);
        log.info("Пользователь успешно обновлен с id: {}", user.getId());
        return user;
    }

    private void setNameIfBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя не указано, используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }

    private void validateLogin(String login) {
        if (login.contains(" ")) {
            log.error("Ошибка валидации: логин содержит пробелы: {}", login);
            throw new ValidationException("Логин не может содержать пробелы");
        }
        log.debug("Валидация пользователя пройдена успешно");
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