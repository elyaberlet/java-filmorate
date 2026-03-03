package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
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
    public User create(@RequestBody User user) {
        log.info("Создание пользователя с логином: {}", user.getLogin());
        validateUser(user);
        setNameIfBlank(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан с id: {}", user.getId());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Обновление пользователя с id: {}", user.getId());

        if (user.getId() == null) {
            log.error("Попытка обновления без указания ID");
            throw new ValidationException("ID не может быть null");
        }

        if (!users.containsKey(user.getId())) {
            log.error("Попытка обновления несуществующего пользователя с id: {}", user.getId());
            throw new ValidationException("Пользователь с id " + user.getId() + " не найден");
        }

        validateUser(user);
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

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Ошибка валидации: email пользователя пустой");
            throw new ValidationException("Email не может быть пустым");
        }
        if (!user.getEmail().contains("@")) {
            log.error("Ошибка валидации: email не содержит @: {}", user.getEmail());
            throw new ValidationException("Email должен содержать @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("Ошибка валидации: логин пользователя пустой");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Ошибка валидации: логин содержит пробелы: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации: некорректная дата рождения: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
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