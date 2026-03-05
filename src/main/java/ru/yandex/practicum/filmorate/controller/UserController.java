package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
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
    public User getUserById(@PathVariable Long id) {
        log.info("Получение пользователя по id: {}", id);
        if (id == null || id <= 0) {
            log.error("Ошибка: некорректный id пользователя: {}", id);
            throw new ValidationException("ID пользователя должно быть положительным числом");
        }
        return userService.findUserById(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getUserFriends(@PathVariable Long id) {
        log.info("Запрос списка друзей пользователя id={}", id);
        if (id == null || id <= 0) {
            log.error("Ошибка: некорректный id пользователя: {}", id);
            throw new ValidationException("ID пользователя должно быть положительным числом");
        }
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Поиск общих друзей между пользователями id={} и id={}", id, otherId);
        if (id == null || id <= 0) {
            log.error("Ошибка: некорректный ID пользователя: {}", id);
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }
        if (otherId == null || otherId <= 0) {
            log.error("Ошибка: некорректный ID другого пользователя: {}", otherId);
            throw new ValidationException("ID другого пользователя должен быть положительным числом");
        }
        if (id.equals(otherId)) {
            log.error("Ошибка: попытка найти общих друзей с самим собой");
            throw new ValidationException("Нельзя искать общих друзей с самим собой");
        }
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Создание нового пользователя с email: {}, логин: {}", user.getEmail(), user.getLogin());
        if (user == null) {
            log.error("Ошибка: тело запроса не может быть пустым");
            throw new ValidationException("Тело запроса не может быть пустым");
        }
        validateUser(user);
        setNameIfBlank(user);
        return userService.createUser(user);
    }

    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Обновление пользователя id={}: email={}, логин={}",
                user.getId(), user.getEmail(), user.getLogin());
        if (user == null) {
            log.error("Ошибка: тело запроса не может быть пустым");
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        if (user.getId() == null) {
            log.error("Ошибка валидации: ID пользователя отсутствует при обновлении");
            throw new ValidationException("ID пользователя не может быть null при обновлении");
        }
        if (user.getId() <= 0) {
            log.error("Ошибка валидации: ID пользователя должен быть положительным числом: {}", user.getId());
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }
        validateUser(user);
        setNameIfBlank(user);
        return userService.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriendById(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Добавление друга: пользователь id={} добавляет в друзья пользователя id={}", id, friendId);
        if (id == null || id <= 0) {
            log.error("Ошибка: некорректный ID пользователя: {}", id);
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }
        if (friendId == null || friendId <= 0) {
            log.error("Ошибка: некорректный ID друга: {}", friendId);
            throw new ValidationException("ID друга должен быть положительным числом");
        }
        if (id.equals(friendId)) {
            log.error("Ошибка: пользователь пытается добавить сам себя в друзья");
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Удаление из друзей: пользователь id={} удаляет из друзей пользователя id={}", id, friendId);
        if (id == null || id <= 0) {
            log.error("Ошибка: некорректный ID пользователя: {}", id);
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }
        if (friendId == null || friendId <= 0) {
            log.error("Ошибка: некорректный ID друга: {}", friendId);
            throw new ValidationException("ID друга должен быть положительным числом");
        }
        if (id.equals(friendId)) {
            log.error("Ошибка: пользователь пытается удалить сам себя из друзей");
            throw new ValidationException("Нельзя удалить самого себя из друзей");
        }
        userService.deleteFriend(id, friendId);
    }

    private void setNameIfBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя не указано, будет использован логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Ошибка валидации: email пользователя отсутствует или пустой");
            throw new ValidationException("Email не может быть пустым");
        }
        if (!user.getEmail().contains("@")) {
            log.error("Ошибка валидации: email '{}' не содержит символ @", user.getEmail());
            throw new ValidationException("Email должен содержать @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("Ошибка валидации: логин пользователя отсутствует или пустой");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Ошибка валидации: логин '{}' содержит пробелы", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации: дата рождения '{}' некорректна (не может быть в будущем)",
                    user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        log.debug("Валидация пользователя {} пройдена успешно", user.getLogin());
    }
}