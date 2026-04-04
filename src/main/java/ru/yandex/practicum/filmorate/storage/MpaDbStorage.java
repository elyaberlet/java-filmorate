package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@AllArgsConstructor
@Repository
public class MpaDbStorage {
    private final JdbcTemplate jdbc;
    private final MpaRowMapper mapper;

    private static final String GET_ALL =
            "SELECT mpa_id, mpa_name FROM mpa ORDER BY mpa_id";

    private static final String GET_BY_ID =
            "SELECT mpa_id, mpa_name FROM mpa WHERE mpa_id = ?";

    public List<Mpa> getAll() {
        return jdbc.query(GET_ALL, mapper);
    }

    public Mpa getById(int id) {
        return jdbc.queryForObject(GET_BY_ID, mapper, id);
    }
}
