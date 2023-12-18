package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.HashSet;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testFindUserById() {
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);

        Film newFilm = Film.builder()
                .id(1)
                .name("film name")
                .likes(new HashSet<>())
                .rating(new Rating(1, "G"))
                .releaseDate(LocalDate.of(1990, 10, 10))
                .description("info")
                .duration(120F)
                .genres(new HashSet<>())
                .build();

        filmStorage.create(newFilm);

        Film savedFilm = filmStorage.findFilmById(1);

        Assertions.assertEquals(newFilm, savedFilm);
    }
}