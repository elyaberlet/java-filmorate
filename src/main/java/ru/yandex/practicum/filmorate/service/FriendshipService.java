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
        log.info("Пользователь {} отправил запрос в друзья {}", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        validateNotSameUser(userId, friendId);
        validateUsersExist(userId, friendId);

        String status = friendshipStorage.getFriendshipStatus(userId, friendId);

        if (status == null) {
            log.info("Пользователь {} пытался удалить несуществующую дружбу с {}", userId, friendId);
            return;
        }

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

    private void validateNotSameUser(Long userId, Long otherUserId) {
        if (userId.equals(otherUserId)) {
            throw new ValidationException("Нельзя выполнять операцию с самим собой");
        }
    }

    private void validateUserExists(Long userId) {
        userStorage.findUserById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private void validateUsersExist(Long firstUserId, Long secondUserId) {
        validateUserExists(firstUserId);
        validateUserExists(secondUserId);
    }
}
