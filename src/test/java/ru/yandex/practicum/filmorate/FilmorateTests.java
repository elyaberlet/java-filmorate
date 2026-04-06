package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.mappers.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@JdbcTest
@AutoConfigureTestDatabase
class FilmorateTests {

    @Autowired
    private JdbcTemplate jdbc;

    private final FilmRowMapper filmRowMapper = new FilmRowMapper();
    private final GenreRowMapper genreRowMapper = new GenreRowMapper();
    private final UserRowMapper userRowMapper = new UserRowMapper();
    private final MpaRowMapper mpaRowMapper = new MpaRowMapper();

    private FriendshipDbStorage friendStorage;
    private FilmDbStorage filmStorage;
    private UserDbStorage userStorage;
    private GenreDbStorage genreStorage;
    private MpaDbStorage mpaStorage;

    private Film film;
    private User user;
    private User user1;
    private long userId;

    @BeforeEach
    void setUp() {
        friendStorage = new FriendshipDbStorage(jdbc, userRowMapper);
        filmStorage = new FilmDbStorage(jdbc, filmRowMapper, genreRowMapper);
        userStorage = new UserDbStorage(jdbc, userRowMapper);
        genreStorage = new GenreDbStorage(jdbc, genreRowMapper);
        mpaStorage = new MpaDbStorage(jdbc, mpaRowMapper);

        user = userStorage.createUser(User.builder()
                .email("u@mail.com")
                .login("user")
                .name("User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build());

        userId = user.getId();

        film = filmStorage.createFilm(Film.builder()
                .name("Test Film")
                .description("Desc")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new Mpa(1, "G"))
                .build());
    }

    @Test
    void testCreateFilm() {
        assertThat(film.getId()).isPositive();
        assertThat(film.getName()).isEqualTo("Test Film");
    }

    @Test
    void testGetFilmById() {
        Film found = filmStorage.findFilmById(film.getId()).orElseThrow();
        assertThat(found.getId()).isEqualTo(film.getId());
    }

    @Test
    void testUpdateFilm() {
        film.setDuration(100);
        Film updated = filmStorage.updateFilm(film);
        assertThat(updated.getDuration()).isEqualTo(100);
    }

    @Test
    void testGetAllFilms() {
        Collection<Film> films = filmStorage.findAllFilms();
        assertThat(films).isNotEmpty();
        assertThat(films.size()).isEqualTo(1);
    }

    @Test
    void testAddAndRemoveLike() {
        filmStorage.addLike(film.getId(), userId);
        Film liked = filmStorage.findFilmById(film.getId()).orElseThrow();
        assertThat(liked.getLikes()).isEqualTo(1);

        filmStorage.removeLike(film.getId(), userId);
        Film unliked = filmStorage.findFilmById(film.getId()).orElseThrow();
        assertThat(unliked.getLikes()).isEqualTo(0);
    }

    @Test
    void testGetPopularFilms() {
        Film second = filmStorage.createFilm(Film.builder()
                .name("Film2")
                .description("D")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build());

        filmStorage.addLike(second.getId(), userId);

        List<Film> popular = filmStorage.getPopular(5);

        assertThat(popular.getFirst().getId()).isEqualTo(second.getId());
    }

    @Test
    void testCreateUser() {
        assertThat(user.getId()).isPositive();
        assertThat(user.getEmail()).isEqualTo("u@mail.com");
    }

    @Test
    void testFindUserById() {
        Optional<User> found = userStorage.findUserById(userId);
        assertThat(found).isPresent();
        assertThat(found.get().getLogin()).isEqualTo("user");
    }

    @Test
    void testUpdateUser() {
        user.setName("Updated");
        User updated = userStorage.updateUser(user);
        assertThat(updated.getName()).isEqualTo("Updated");
    }

    @Test
    void testGetAllUsers() {
        List<User> users = (List<User>) userStorage.getAllUsers();
        assertThat(users).isNotEmpty();
        assertThat(users.size()).isEqualTo(1);
    }

    @Test
    void testDeleteUser() {
        boolean deleted = userStorage.deleteUser(userId);
        assertThat(deleted).isTrue();

        Optional<User> found = userStorage.findUserById(userId);
        assertThat(found).isEmpty();
    }

    @Test
    void testAddFriend() {
        user1 = userStorage.createUser(User.builder()
                .email("u1@mail.com")
                .login("user1")
                .name("User1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        friendStorage.addFriend(user.getId(), user1.getId());

        List<User> friends = friendStorage.getFriends(user.getId());
        assertThat(friends)
                .as("Список друзей пользователя должен содержать 1 человека после добавления")
                .hasSize(1);

        assertThat(friends.get(0).getId())
                .isEqualTo(user1.getId());
    }

    @Test
    void testRemoveFriend() {
        user1 = userStorage.createUser(User.builder()
                .email("u1@mail.com")
                .login("user1")
                .name("User1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        friendStorage.addFriend(user1.getId(), user.getId());
        friendStorage.addFriend(user.getId(), user1.getId());

        friendStorage.removeFriend(user1.getId(), user.getId());

        List<User> friends = friendStorage.getFriends(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        user1 = userStorage.createUser(User.builder()
                .email("u1@mail.com")
                .login("user1")
                .name("User1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        User user2 = userStorage.createUser(User.builder()
                .email("u3@mail.com")
                .login("user3")
                .name("User3")
                .birthday(LocalDate.of(1992, 1, 1))
                .build());

        friendStorage.addFriend(user1.getId(), user2.getId());
        friendStorage.addFriend(user2.getId(), user1.getId());

        friendStorage.addFriend(user.getId(), user2.getId());
        friendStorage.addFriend(user2.getId(), user.getId());

        List<User> common = friendStorage.getCommonFriends(user1.getId(), user.getId());

        assertThat(common).hasSize(1);
        assertThat(common.getFirst().getId()).isEqualTo(user2.getId());
    }

    @Test
    void testFindAllGenres() {
        List<Genre> genres = genreStorage.getAllGenres();
        assertThat(genres).isNotEmpty();
        assertThat(genres.size()).isEqualTo(6);
    }

    @Test
    void testGetGenreById() {
        Genre genre = genreStorage.getGenreById(2);
        assertThat(genre.getId()).isEqualTo(2);
        assertThat(genre.getName()).isEqualTo("Драма");
    }

    @Test
    void testGetAllMpa() {
        List<Mpa> mpa = mpaStorage.getAll();
        assertThat(mpa).isNotEmpty();
        assertThat(mpa.size()).isEqualTo(5);
    }

    @Test
    void testGetMpaById() {
        Mpa mpa = mpaStorage.getById(3);
        assertThat(mpa.getId()).isEqualTo(3);
        assertThat(mpa.getName()).isEqualTo("PG-13");
    }
}
