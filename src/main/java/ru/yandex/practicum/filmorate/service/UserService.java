package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(int id) {
        return userStorage.findUserById(id);
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void addFriend(int userId, int friendId) {
        User user = userStorage.findUserById(userId);
        User friend = userStorage.findUserById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.debug("Added friends: {} and {}", userId, friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        User user = userStorage.findUserById(userId);
        User friend = userStorage.findUserById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.debug("Deleted friends: {} and {}", userId, friendId);
    }

    public List<User> getFriends(int id) {
        return userStorage.findUserById(id).getFriends().stream()
                .map(userStorage::findUserById).collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int friendId) {
        List<User> users = userStorage.findAll();
        return users.stream().filter(x ->
                !x.getFriends().isEmpty() && x.getFriends().contains(userId) && x.getFriends().contains(friendId)).collect(Collectors.toList());
    }
}
