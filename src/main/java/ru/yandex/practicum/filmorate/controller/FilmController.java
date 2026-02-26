package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAllFilms() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Создание фильма: {}", film.getName());
        validateFilm(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно создан с id: {}", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Обновление фильма с id: {}", film.getId());

        if (film.getId() == null) {
            log.error("Попытка обновления без указания ID");
            throw new ValidationException("ID не может быть null");
        }

        if (!films.containsKey(film.getId())) {
            log.error("Попытка обновления несуществующего фильма с id: {}", film.getId());
            throw new ValidationException("Фильм с id " + film.getId() + " не найден");
        }

        validateFilm(film);
        films.put(film.getId(), film);
        log.info("Фильм успешно обновлен с id: {}", film.getId());
        return film;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: название фильма пустое");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: описание фильма слишком длинное ({} символов)", film.getDescription().length());
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            log.error("Ошибка валидации: некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.error("Ошибка валидации: некорректная продолжительность фильма");
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }
        log.debug("Валидация фильма пройдена успешно");
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}