package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

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
        userStorage.findUserById(friendId); // check friend exists
        user.getFriendIds().add(friendId);
        userStorage.update(user);
        log.debug("Added friends: {} and {}", userId, friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        User user = userStorage.findUserById(userId);
        userStorage.findUserById(friendId); // check friend exists
        user.getFriendIds().remove(friendId);
        userStorage.update(user);
        log.debug("Deleted friends: {} and {}", userId, friendId);
    }

    public List<User> getFriends(int id) {
        return userStorage.findUserById(id).getFriendIds().stream()
                .map(userStorage::findUserById).collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        Set<Integer> userFriendIds = userStorage.findUserById(userId).getFriendIds();
        Set<Integer> otherFriendIds = userStorage.findUserById(otherId).getFriendIds();

        Set<Integer> commonFriendIds = userFriendIds.stream().filter(otherFriendIds::contains).collect(Collectors.toSet());

        List<User> users = commonFriendIds.stream().map(userStorage::findUserById).collect(Collectors.toList());
        log.debug("Common friends {} and {} - {}", userId, otherId, users.toString());
        return users;
    }
}
