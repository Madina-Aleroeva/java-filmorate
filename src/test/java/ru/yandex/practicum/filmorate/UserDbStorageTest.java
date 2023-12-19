package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testFindUserById() {
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        User newUser = User.builder()
                .id(1)
                .email("user@mail.ru")
                .login("user")
                .name("name of user")
                .birthday(LocalDate.of(1990, 10, 10))
                .friendIds(new HashSet<>())
                .build();

        userStorage.create(newUser);

        User savedUser = userStorage.findUserById(1);

        Assertions.assertEquals(newUser, savedUser);
    }
}
