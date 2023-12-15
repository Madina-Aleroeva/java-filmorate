package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

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

    private void setNewFilmId(Film film) {
        int id = 0;
        if (!films.isEmpty()) {
            id = Collections.max(films.keySet());
        }
        log.debug("Set film id {}", id + 1);
        film.setId(id + 1);
    }

    private void checkCorrect(Film film) {
        StringBuilder sb = new StringBuilder();

        if (film.getName().isBlank()) {
            sb.append("Name shouldn't be blank.");
        }
        if (film.getDescription().length() > 200) {
            sb.append("Description should be less than 200 symbols.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            sb.append("Release date should be from 28 december 1895.");
        }
        if (film.getDuration() <= 0) {
            sb.append("Duration should be positive value.");
        }

        if (sb.length() > 0) {
            log.debug(sb.toString());
            throw new ValidationException(sb.toString());
        }

    }
}
