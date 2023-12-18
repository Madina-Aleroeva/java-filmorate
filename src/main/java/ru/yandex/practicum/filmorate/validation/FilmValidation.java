package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Component
@Slf4j
public class FilmValidation {
    public static void checkCorrect(Film film) {
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
