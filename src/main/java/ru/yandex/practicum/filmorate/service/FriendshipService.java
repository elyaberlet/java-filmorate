package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final UserDbStorage userStorage;
    private final FriendshipDbStorage friendshipStorage;

    public void addFriend(Long userId, Long friendId) {
        validateNotSameUser(userId, friendId);
        validateUsersExist(userId, friendId);

        friendshipStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        validateNotSameUser(userId, friendId);
        validateUsersExist(userId, friendId);

        Integer status = friendshipStorage.getFriendshipStatus(userId, friendId);

        if (status == null) return;

        friendshipStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей {}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        validateUserExists(userId);
        return friendshipStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        validateNotSameUser(userId, otherId);
        validateUsersExist(userId, otherId);

        return friendshipStorage.getCommonFriends(userId, otherId);
    }

    private void validateNotSameUser(Long id, Long otherId) {
        if (id.equals(otherId)) {
            throw new ValidationException("Нельзя выполнять операцию с самим собой");
        }
    }

    private void validateUserExists(Long id) {
        userStorage.findUserById(id)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    private void validateUsersExist(Long id1, Long id2) {
        validateUserExists(id1);
        validateUserExists(id2);
    }
}
