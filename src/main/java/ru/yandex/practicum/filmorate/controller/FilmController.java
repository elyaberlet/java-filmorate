package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Получение фильма по id: {}", id);
        if (id == null || id <= 0) {
            log.error("Ошибка: некорректный ID фильма: {}", id);
            throw new ValidationException("ID фильма должен быть положительным числом");
        }
        return filmService.findFilmById(id);
    }

    @GetMapping("/popular")
    public List<Film> getMostPopularFilm(@RequestParam(defaultValue = "10") int count) {
        if (count <= 0) {
            log.error("Ошибка: некорректное количество фильмов: {}", count);
            throw new ValidationException("Количество фильмов должно быть положительным числом");
        }
        log.info("Запрос топ-{} популярных фильмов", count);
        return filmService.getPopularFilms(count);
    }

    @GetMapping
    public Collection<Film> findAllFilms() {
        log.info("Запрос списка всех фильмов");
        return filmService.findAllFilms();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        if (film == null) {
            log.error("Ошибка: тело запроса не может быть пустым");
            throw new ValidationException("Тело запроса не может быть пустым");
        }
        log.info("Создание нового фильма: '{}'", film.getName());
        validateFilm(film);
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        if (film == null) {
            log.error("Ошибка: тело запроса не может быть пустым");
            throw new ValidationException("Тело запроса не может быть пустым");
        }
        if (film.getId() == null) {
            log.error("Ошибка валидации: ID фильма отсутствует при обновлении");
            throw new ValidationException("ID фильма не может быть null при обновлении");
        }
        if (film.getId() <= 0) {
            log.error("Ошибка валидации: ID фильма должен быть положительным числом: {}", film.getId());
            throw new ValidationException("ID фильма должен быть положительным числом");
        }
        log.info("Обновление фильма id={}: '{}'", film.getId(), film.getName());
        validateFilm(film);
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        if (id == null || id <= 0) {
            log.error("Ошибка: некорректный ID фильма: {}", id);
            throw new ValidationException("ID фильма должен быть положительным числом");
        }
        if (userId == null || userId <= 0) {
            log.error("Ошибка: некорректный ID пользователя: {}", userId);
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }
        log.info("Пользователь id={} ставит лайк фильму id={}", userId, id);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        if (id == null || id <= 0) {
            log.error("Ошибка: некорректный ID фильма: {}", id);
            throw new ValidationException("ID фильма должен быть положительным числом");
        }
        if (userId == null || userId <= 0) {
            log.error("Ошибка: некорректный ID пользователя: {}", userId);
            throw new ValidationException("ID пользователя должен быть положительным числом");
        }
        log.info("Пользователь id={} удаляет лайк у фильма id={}", userId, id);
        filmService.removeLike(id, userId);
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: название фильма отсутствует или пустое");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: описание фильма превышает 200 символов (длина: {})", film.getDescription().length());
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            log.error("Ошибка валидации: дата релиза {} некорректна (минимальная дата: {})",
                    film.getReleaseDate(), EARLIEST_RELEASE_DATE);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.error("Ошибка валидации: продолжительность фильма {} некорректна", film.getDuration());
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }
        log.debug("Валидация фильма '{}' пройдена успешно", film.getName());
    }
}