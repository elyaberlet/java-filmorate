package ru.yandex.practicum.filmorate.exception;

public class SelfFriendshipNotAllowed extends RuntimeException {
    public SelfFriendshipNotAllowed(String message) {
        super(message);
    }
}
