package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    private static final String GET_ALL_GENRES = "SELECT * FROM genre ORDER BY genre_id";
    private static final String GET_GENRE_BY_ID = "SELECT * FROM genre WHERE genre_id = ?";

    public List<Genre> getAllGenres() {
        return jdbc.query(GET_ALL_GENRES, mapper);
    }

    public Genre getGenreById(int id) {
        return jdbc.queryForObject(GET_GENRE_BY_ID, mapper, id);
    }
}
