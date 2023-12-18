package ru.yandex.practicum.filmorate.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
@Slf4j
public class RatingService {
    private final FilmStorage filmStorage;

    public RatingService(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public List<Rating> findAllRatings() {
        return filmStorage.findAllRatings();
    }

    public Rating findRatingById(int id) {
        return filmStorage.findRatingById(id);
    }

}
