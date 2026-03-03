package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {
    private FilmController filmController;
    private UserController userController;
    private User user;
    private Film film;

    @BeforeEach
    public void setUp() {
        filmController = new FilmController();
        film = new Film();
        film.setName("Грозовой перевал");
        film.setDescription("Экранизация по виденью режиссера");
        film.setDuration(120);
        film.setReleaseDate(LocalDate.of(2026, 2, 16));
        userController = new UserController();
        user = new User();
        user.setId(1L);
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user.setLogin("Login");
        user.setEmail("@testEmail.com");
    }

    // POST

    @Test
    void shouldCreateUserIfValid() {
        User created = userController.create(user);
        assertNotNull(created.getId());
        assertEquals(user.getId(), created.getId());
        assertEquals(user.getName(), created.getName());
        assertEquals(user.getLogin(), created.getLogin());
        assertEquals(user.getBirthday(), created.getBirthday());
    }

    @Test
    void shouldCreateUserWhenNameIsNull() {
        user.setName(null);
        User created = userController.create(user);
        assertNotNull(created.getId());
        assertEquals(user.getId(), created.getId());
        assertEquals(user.getLogin(), created.getName());
        assertEquals(user.getLogin(), created.getLogin());
        assertEquals(user.getBirthday(), created.getBirthday());
    }

    @Test
    void shouldThrowExceptionWhenEmailEmpty() {
        user.setEmail(null);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Email не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenBlankEmpty() {
        user.setEmail(" ");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Email не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailWithoutSymbol() {
        user.setEmail("testEmail.com");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Email должен содержать @", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmptyLogin() {
        user.setLogin(null);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Логин не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenBlankLogin() {
        user.setLogin(" ");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Логин не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfLoginWithSpaces() {
        user.setLogin("ло гин");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Логин не может содержать пробелы", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFutureBirthday() {
        user.setBirthday(LocalDate.now().plusDays(1));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void shouldCreateUserIfTodayBirthday() {
        user.setBirthday(LocalDate.now());
        User created = userController.create(user);
        assertEquals(LocalDate.now(), created.getBirthday());

    }

    @Test
    public void shouldCreateFilmWhenValid() {
        Film created = filmController.create(film);
        assertNotNull(created.getId());
        assertEquals(1L, created.getId());
        assertEquals(film.getName(), created.getName());
        assertEquals(film.getDescription(), created.getDescription());
        assertEquals(film.getReleaseDate(), created.getReleaseDate());
        assertEquals(film.getDuration(), created.getDuration());
    }

    @Test
    public void shouldThrowExceptionWhenNameIsNull() {
        film.setName(null);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void create_ShouldThrowExceptionWhenNameIsBlank() {
        film.setName(" ");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void create_ShouldThrowExceptionWhenNameIsEmpty() {
        film.setName("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDescriptionTooLong() {
        film.setDescription("a".repeat(201));

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Описание не может быть длиннее 200 символов", exception.getMessage());
    }

    @Test
    void shouldAddFilmWhenDescription200Chars() {
        String filmDescription = "a".repeat(200);
        film.setDescription(filmDescription);
        Film created = filmController.create(film);
        assertEquals(filmDescription, created.getDescription());
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateBeforeEarliestDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNull() {
        film.setDuration(null);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Продолжительность должна быть положительным числом", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDurationIsZero() {
        film.setDuration(0);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Продолжительность должна быть положительным числом", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        film.setDuration(-10);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Продолжительность должна быть положительным числом", exception.getMessage());
    }

    // PUT
    @Test
    void shouldUpdateUserIfValid() {
        User createdUser = userController.create(user);

        User updatedUser = new User();
        updatedUser.setId(createdUser.getId());
        updatedUser.setEmail("updated@test.com");
        updatedUser.setLogin("updatedLogin");
        updatedUser.setName("Updated");
        updatedUser.setBirthday(LocalDate.of(1995, 1, 1));

        User result = userController.update(updatedUser);

        assertEquals(updatedUser.getEmail(), result.getEmail());
        assertEquals(updatedUser.getLogin(), result.getLogin());
        assertEquals(updatedUser.getName(), result.getName());
        assertEquals(updatedUser.getBirthday(), result.getBirthday());
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithNullId() {
        User userWithNullId = new User();
        userWithNullId.setEmail("test@test.com");
        userWithNullId.setLogin("testlogin");
        userWithNullId.setName("Test User");
        userWithNullId.setBirthday(LocalDate.of(1999, 1, 31));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.update(userWithNullId));
        assertEquals("ID не может быть null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenIdNotFound() {
        user.setId(9L);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.update(user));
        assertEquals("Пользователь с id 9 не найден", exception.getMessage());
    }

    @Test
    void updateWithBlankNameShouldSetNameToLogin() {
        user.setName(null);
        User created = userController.create(user);
        assertEquals(user.getLogin(), created.getName());
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        film.setId(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.update(film));
        assertEquals("ID не может быть null", exception.getMessage());
    }

    @Test
    void shouldReturnExceptionWhenFilmNotFound() {
        film.setId(777L);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.update(film));
        assertEquals("Фильм с id 777 не найден", exception.getMessage());
    }
}


