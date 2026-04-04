package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendshipService;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/users")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(
            @PathVariable @Positive Long id,
            @PathVariable @Positive Long friendId) {

        log.info("Пользователь {} добавляет в друзья {}", id, friendId);
        friendshipService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(
            @PathVariable @Positive Long id,
            @PathVariable @Positive Long friendId) {

        log.info("Пользователь {} удаляет из друзей {}", id, friendId);
        friendshipService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(
            @PathVariable @Positive Long id) {

        log.info("Запрос друзей пользователя {}", id);
        return friendshipService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(
            @PathVariable @Positive Long id,
            @PathVariable @Positive Long otherId) {

        log.info("Запрос общих друзей между {} и {}", id, otherId);
        return friendshipService.getCommonFriends(id, otherId);
    }
}
