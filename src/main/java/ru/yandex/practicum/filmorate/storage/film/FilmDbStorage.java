package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.LinkedHashSet;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;

    private static final String INSERT_FILM_SQL =
            "INSERT INTO film (name, description, release_date, duration, mpa_id) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_FILM_SQL =
            "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                    "WHERE film_id = ?";

    private static final String GET_FILM_SQL =
            "SELECT f.*, m.mpa_name " +
                    "FROM film f " +
                    "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                    "WHERE f.film_id = ?";

    private static final String GET_ALL_FILMS_SQL =
            "SELECT f.*, m.mpa_name " +
                    "FROM film f " +
                    "JOIN mpa m ON f.mpa_id = m.mpa_id";

    private static final String GET_GENRES_BY_FILM_SQL =
            "SELECT g.genre_id, g.genre_name " +
                    "FROM film_genre fg " +
                    "JOIN genre g ON fg.genre_id = g.genre_id " +
                    "WHERE fg.film_id = ? " +
                    "ORDER BY g.genre_id";

    private static final String INSERT_FILM_GENRE_SQL =
            "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

    private static final String DELETE_FILM_GENRES_SQL =
            "DELETE FROM film_genre WHERE film_id = ?";

    private static final String COUNT_LIKES_SQL =
            "SELECT COUNT(user_id) FROM film_like WHERE film_id = ?";

    public Film create(Film film) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_FILM_SQL, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);

        saveGenres(film);

        return getFilmById(filmId);
    }

    public Film update(Film film) {

        jdbc.update(UPDATE_FILM_SQL,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        jdbc.update(DELETE_FILM_GENRES_SQL, film.getId());
        saveGenres(film);

        return getFilmById(film.getId());
    }

    public Film getFilmById(long id) {
        Film film = jdbc.queryForObject(GET_FILM_SQL, filmRowMapper, id);

        List<Genre> genres = jdbc.query(GET_GENRES_BY_FILM_SQL, genreRowMapper, id);
        film.setGenres(new LinkedHashSet<>(genres));

        film.setLikes(getLikes(id));

        return film;
    }

    public List<Film> getAll() {
        List<Film> films = jdbc.query(GET_ALL_FILMS_SQL, filmRowMapper);

        for (Film film : films) {
            List<Genre> genres = jdbc.query(GET_GENRES_BY_FILM_SQL, genreRowMapper, film.getId());
            film.setGenres(new LinkedHashSet<>(genres));

            film.setLikes(getLikes(film.getId()));
        }

        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null) return;

        for (Genre genre : film.getGenres()) {
            jdbc.update(INSERT_FILM_GENRE_SQL, film.getId(), genre.getId());
        }
    }

    private int getLikes(long filmId) {
        return jdbc.queryForObject(COUNT_LIKES_SQL, Integer.class, filmId);
    }

    public void addLike(long filmId, long userId) {
        String sql = "INSERT INTO film_like (film_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        String sql = "DELETE FROM film_like WHERE film_id = ? AND user_id = ?";
        jdbc.update(sql, filmId, userId);
    }

    public List<Film> getPopular(int count) {
        String sql =
                "SELECT f.*, m.mpa_name " +
                        "FROM film f " +
                        "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                        "LEFT JOIN film_like fl ON f.film_id = fl.film_id " +
                        "GROUP BY f.film_id, m.mpa_name " +
                        "ORDER BY COUNT(fl.user_id) DESC " +
                        "LIMIT ?";

        List<Film> films = jdbc.query(sql, filmRowMapper, count);

        for (Film film : films) {
            List<Genre> genres = jdbc.query(GET_GENRES_BY_FILM_SQL, genreRowMapper, film.getId());
            film.setGenres(new LinkedHashSet<>(genres));

            film.setLikes(getLikes(film.getId()));
        }
        return films;
    }
}
