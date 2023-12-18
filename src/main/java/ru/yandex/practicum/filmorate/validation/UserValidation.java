package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
@Component
public class UserValidation {
    public static void checkCorrect(User user) {
        StringBuilder sb = new StringBuilder();

        if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            sb.append("Email shouldn't be blank and should contain '@'.");
        }
        if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            sb.append("Login shouldn't be blank and shouldn't contain spaces.");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            sb.append("Birthdate shouldn't be in the future.");
        }

        if (sb.length() > 0) {
            log.debug(sb.toString());
            throw new ValidationException(sb.toString());
        }
    }
}
