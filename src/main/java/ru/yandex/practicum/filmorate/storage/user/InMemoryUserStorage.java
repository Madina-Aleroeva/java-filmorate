package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public User findUserById(int userId) {
        if (!users.containsKey(userId))
            throw new NotFoundException("User with id = " + userId + " not found");
        return users.get(userId);
    }

    @Override
    public User create(User user) {
        setNewUserId(user);
        user.setFriends(new HashSet<>());
        putUser(user);
        log.debug("Created user {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("User with id " + user.getId() + " not exists");
        }
        if (user.getFriends() == null)
            user.setFriends(new HashSet<>());
        putUser(user);
        log.debug("Updated user {}", user);
        return user;
    }

    private void putUser(User user) {
        checkCorrect(user);
        setNameIfNullOrBlank(user);
        users.put(user.getId(), user);
    }

    private void checkCorrect(User user) {
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

    private void setNewUserId(User user) {
        int id = 0;
        if (!users.isEmpty()) {
            id = Collections.max(users.keySet());
        }
        log.debug("Set user id {}", id);
        user.setId(id + 1);
    }

    private void setNameIfNullOrBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Set user name of login {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}
