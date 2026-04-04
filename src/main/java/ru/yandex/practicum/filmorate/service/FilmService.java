package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class FilmService {
    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;

    public void addLike(Long filmId, Long userId) {
        var film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        filmStorage.addLike(filmId, userId);
    }

    public Collection<Film> findAllFilms() {
        return filmStorage.getAll();
    }

    public Film createFilm(Film film) {

        try {
            mpaStorage.getById(film.getMpa().getId());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                try {
                    genreStorage.getGenreById(genre.getId());
                } catch (EmptyResultDataAccessException e) {
                    throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
                }
            }
        }
        validateFilm(film);
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        if (film == null) {
            log.error("Ошибка: тело запроса не может быть пустым");
            throw new ValidationException("Тело запроса не может быть пустым");
        }
        if (film.getId() <= 0) {
            log.error("Ошибка валидации: ID фильма должен быть положительным числом: {}", film.getId());
            throw new ValidationException("ID фильма должен быть положительным числом");
        }
        validateFilm(film);
        return filmStorage.update(film);
    }

    public Film findFilmById(Long id) {
        try {
            return filmStorage.getFilmById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
    }


    public void removeLike(Long filmId, Long userId) {
        filmStorage.getFilmById(filmId);
        userStorage.findUserById(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopular(count);
    }

    public void validateFilm(Film film) {
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