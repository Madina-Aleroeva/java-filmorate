package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.*;

import static ru.yandex.practicum.filmorate.validation.FilmValidation.checkCorrect;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film findFilmById(int id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Film with id = " + id + " not found");
        }
        return films.get(id);
    }

    @Override
    public Film create(Film film) {
        checkCorrect(film);
        setNewFilmId(film);
        film.setLikes(new HashSet<>());
        films.put(film.getId(), film);
        log.debug("Created film {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Film with id " + film.getId() + " not exists");
        }
        checkCorrect(film);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        films.put(film.getId(), film);
        log.debug("Updated film {}", film);
        return film;
    }

    @Override
    public List<Genre> findAllGenres() {
        return null;
    }

    @Override
    public Genre findGenreById(int id) {
        return null;
    }

    @Override
    public List<Rating> findAllRatings() {
        return null;
    }

    @Override
    public Rating findRatingById(int id) {
        return null;
    }

    private void setNewFilmId(Film film) {
        int id = 0;
        if (!films.isEmpty()) {
            id = Collections.max(films.keySet());
        }
        log.debug("Set film id {}", id + 1);
        film.setId(id + 1);
    }


}
