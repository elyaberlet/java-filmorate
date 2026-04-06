package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.*;

@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final GenreRowMapper genreMapper;

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper, GenreRowMapper genreMapper) {
        super(jdbc, mapper);
        this.jdbc = jdbc;
        this.genreMapper = genreMapper;
    }

    private static final String INSERT_FILM =
            "INSERT INTO film (name, description, release_date, duration, mpa_id) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_FILM =
            "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                    "WHERE film_id = ?";

    private static final String SELECT_ALL =
            "SELECT f.*, m.mpa_name, COUNT(fl.user_id) AS likes " +
                    "FROM film f " +
                    "LEFT JOIN film_like fl ON f.film_id = fl.film_id " +
                    "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                    "GROUP BY f.film_id, m.mpa_name";

    private static final String SELECT_BY_ID =
            "SELECT f.*, m.mpa_name, COUNT(fl.user_id) AS likes " +
                    "FROM film f " +
                    "LEFT JOIN film_like fl ON f.film_id = fl.film_id " +
                    "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                    "WHERE f.film_id = ? " +
                    "GROUP BY f.film_id, m.mpa_name";

    private static final String SELECT_GENRES =
            "SELECT g.genre_id, g.genre_name " +
                    "FROM film_genre fg " +
                    "JOIN genre g ON fg.genre_id = g.genre_id " +
                    "WHERE fg.film_id = ? " +
                    "ORDER BY g.genre_id";

    private static final String INSERT_GENRE =
            "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

    private static final String DELETE_GENRES =
            "DELETE FROM film_genre WHERE film_id = ?";

    private static final String DELETE_FILM =
            "DELETE FROM film WHERE film_id = ?";

    private static final String ADD_LIKE =
            "INSERT INTO film_like (film_id, user_id) VALUES (?, ?)";

    private static final String REMOVE_LIKE =
            "DELETE FROM film_like WHERE film_id = ? AND user_id = ?";

    private static final String GET_POPULAR =
            "SELECT f.*, m.mpa_name, COUNT(fl.user_id) AS likes " +
                    "FROM film f " +
                    "LEFT JOIN film_like fl ON f.film_id = fl.film_id " +
                    "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                    "GROUP BY f.film_id, m.mpa_name " +
                    "ORDER BY likes DESC " +
                    "LIMIT ?";

    @Override
    public Film createFilm(Film film) {
        long id = insert(
                INSERT_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );

        saveGenres(id, film);

        Film created = findFilmById(id).orElseThrow();
        created.setGenres(loadGenres(id));
        return created;
    }

    @Override
    public Film updateFilm(Film film) {
        update(
                UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        jdbc.update(DELETE_GENRES, film.getId());
        saveGenres(film.getId(), film);

        Film updated = findFilmById(film.getId()).orElseThrow();
        updated.setGenres(loadGenres(film.getId()));
        return updated;
    }

    private void saveGenres(long filmId, Film film) {
        if (film.getGenres() == null) return;

        film.getGenres().stream()
                .distinct()
                .forEach(g -> jdbc.update(INSERT_GENRE, filmId, g.getId()));
    }

    private Set<Genre> loadGenres(long filmId) {
        return new LinkedHashSet<>(jdbc.query(SELECT_GENRES, genreMapper, filmId));
    }

    @Override
    public Collection<Film> findAllFilms() {
        List<Film> films = findMany(SELECT_ALL);
        films.forEach(f -> f.setGenres(loadGenres(f.getId())));
        return films;
    }

    @Override
    public Optional<Film> findFilmById(Long id) {
        Optional<Film> film = findOne(SELECT_BY_ID, id);
        film.ifPresent(f -> f.setGenres(loadGenres(id)));
        return film;
    }

    @Override
    public boolean deleteFilm(long id) {
        return delete(DELETE_FILM, id);
    }

    @Override
    public List<Film> getPopular(int count) {
        List<Film> films = findMany(GET_POPULAR, count);
        films.forEach(f -> f.setGenres(loadGenres(f.getId())));
        return films;
    }

    @Override
    public void addLike(long filmId, long userId) {
        jdbc.update(ADD_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        jdbc.update(REMOVE_LIKE, filmId, userId);
    }
}
