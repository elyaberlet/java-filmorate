package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable
                            @NotNull(message = "ID фильма не может быть пустым")
                            @Positive(message = "ID фильма должен быть положительным числом")
                            Long id) {
        log.info("Получение фильма по id: {}", id);
        return filmService.findFilmById(id);
    }

    @GetMapping("/popular")
    public List<Film> getMostPopularFilm(@RequestParam(defaultValue = "10")
                                         @Positive(message = "Количество фильмов должно быть положительным числом")
                                         int count) {
        log.info("Запрос топ-{} популярных фильмов", count);
        return filmService.getPopularFilms(count);
    }

    @GetMapping
    public Collection<Film> findAllFilms() {
        log.info("Запрос списка всех фильмов");
        return filmService.findAllFilms();
    }

    @PostMapping
    public Film create(@RequestBody
                       @Valid
                       Film film) {
        log.info("Создание нового фильма: '{}'", film.getName());
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film update(@RequestBody
                       @Valid
                       Film film) {
        log.info("Обновление фильма id={}: '{}'", film.getId(), film.getName());
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(
            @PathVariable @Positive(message = "ID фильма должен быть положительным числом") Long id,
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом") Long userId) {

        log.info("Пользователь {} ставит лайк фильму {}", userId, id);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(
            @PathVariable @Positive(message = "ID фильма должен быть положительным числом") Long id,
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом") Long userId) {

        log.info("Пользователь {} удаляет лайк у фильма {}", userId, id);
        filmService.removeLike(id, userId);
    }
}