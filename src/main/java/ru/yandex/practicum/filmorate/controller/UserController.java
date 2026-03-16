package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAllUsers() {
        log.info("Запрос списка всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable @Positive(message = "ID пользователя должен быть положительным числом") Long id) {
        log.info("Получение пользователя по id: {}", id);
        return userService.findUserById(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getUserFriends(@PathVariable @Positive(message = "ID пользователя должен быть положительным числом") Long id) {
        log.info("Запрос списка друзей пользователя id={}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом") Long id,
            @PathVariable @Positive(message = "ID другого пользователя должен быть положительным числом") Long otherId) {

        log.info("Поиск общих друзей между пользователями id={} и id={}", id, otherId);

        if (id.equals(otherId)) {
            log.error("Ошибка: попытка найти общих друзей с самим собой");
            throw new ValidationException("Нельзя искать общих друзей с самим собой");
        }

        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Создание нового пользователя с email: {}, логин: {}", user.getEmail(), user.getLogin());

        userService.setNameIfBlank(user);

        return userService.createUser(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Обновление пользователя id={}: email={}, логин={}",
                user.getId(), user.getEmail(), user.getLogin());

        if (user.getId() == null) {
            log.error("Ошибка валидации: ID пользователя отсутствует при обновлении");
            throw new ValidationException("ID пользователя не может быть null при обновлении");
        }

        userService.setNameIfBlank(user);

        return userService.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriendById(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом") Long id,
            @PathVariable @Positive(message = "ID друга должен быть положительным числом") Long friendId) {

        log.info("Добавление друга: пользователь {} добавляет в друзья {}", id, friendId);

        userService.validateNotSameUser(id, friendId, "добавить самого себя в друзья");

        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом") Long id,
            @PathVariable @Positive(message = "ID друга должен быть положительным числом") Long friendId) {

        log.info("Удаление из друзей: пользователь {} удаляет из друзей {}", id, friendId);

        userService.validateNotSameUser(id, friendId, "удалить самого себя из друзей");

        userService.deleteFriend(id, friendId);
    }
}