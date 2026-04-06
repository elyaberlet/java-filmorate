package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film createFilm(Film film);

    Film updateFilm(Film film);

    Collection<Film> findAllFilms();

    Optional<Film> findFilmById(Long id);

    boolean deleteFilm(long id);

    List<Film> getPopular(int count);

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);
}
