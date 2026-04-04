package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/genres")
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public Collection<Genre> findAllGenres() {
        log.info("Запрос списка всех жанров");
        return genreService.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre findGenreById(@PathVariable @Positive(message = "ID жанра должен быть положительным числом") int id) {
        log.info("Получение жанра по id: {}", id);
        return genreService.getGenreById(id);
    }
}
