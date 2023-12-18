package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;

public interface FilmStorage {
    List<Film> findAll();

    Film findFilmById(int id);

    Film create(Film film);

    Film update(Film film);

    List<Genre> findAllGenres();

    Genre findGenreById(int id);

    List<Rating> findAllRatings();

    Rating findRatingById(int id);

}
