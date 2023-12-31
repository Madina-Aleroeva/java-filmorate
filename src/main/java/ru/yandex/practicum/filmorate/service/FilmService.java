package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findFilmById(int id) {
        return filmStorage.findFilmById(id);
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public void addLike(int filmId, int userId) {
        Film film = filmStorage.findFilmById(filmId);
        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Can't set like for same film");
        }
        film.getLikes().add(userId);
        log.debug("Added like to filmId = {} from userId = {}", filmId, userId);
        log.debug("Now film has likes: {}", film.getLikes());
    }

    public void deleteLike(int filmId, int userId) {
        Film film = filmStorage.findFilmById(filmId);
        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException("Can't delete like for this film");
        }
        film.getLikes().remove(userId);
        log.debug("Deleted like to filmId = {} from userId = {}", filmId, userId);
        log.debug("Now film has likes: {}", film.getLikes());
    }

    public List<Film> topPopularFilms(int count) {
        List<Film> films = filmStorage.findAll();
        films.sort(((a, b) -> b.getLikes().size() - a.getLikes().size()));
        log.debug("Sorted popular films {}", films);
        return films.stream().limit(count).collect(Collectors.toList());
    }
}
