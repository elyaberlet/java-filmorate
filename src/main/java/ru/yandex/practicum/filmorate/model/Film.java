package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@NoArgsConstructor
@Builder
@Data
@AllArgsConstructor
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    private Long genreId;

    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность не может быть пустой")
    @Positive(message = "Продолжительность должна быть положительной")
    private Integer duration;

    private int likes;

    private Mpa mpa;

    private Set<Genre> genres;
}




