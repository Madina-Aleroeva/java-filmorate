package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static ru.yandex.practicum.filmorate.validation.UserValidation.checkCorrect;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> findAll() {
        Map<Integer, User> users = new HashMap<>();

        String sql = "select * from users left join friend on users.id = friend.req_from";
        jdbcTemplate.query(sql, rs -> {
            mapRowToUser(rs, users);
        });

        return new ArrayList<>(users.values());
    }

    private User createUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getInt("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .friendIds(new HashSet<>())
                .build();
    }

    private void mapRowToUser(ResultSet rs, Map<Integer, User> users) throws SQLException {
        int userId = rs.getInt("users.id");

        if (!users.containsKey(userId)) {
            users.put(userId, createUser(rs));
        }
        User user = users.get(userId);

        // friendId = 0 - значит друзей у пользователя нет
        int friendId = rs.getInt("req_to");
        if (friendId != 0) {
            user.getFriendIds().add(friendId);
        }
    }

    public User findUserById(int userId) {
        Map<Integer, User> users = new HashMap<>();

        String sql = "select * from users left join friend on users.id = req_from where users.id = ?";
        jdbcTemplate.query(sql, rs -> {
            mapRowToUser(rs, users);
        }, userId);

        if (users.isEmpty()) {
            throw new NotFoundException("User with id = " + userId + " not found");
        }

        log.debug("Found user: " + users.get(userId));
        return users.get(userId);
    }

    @Override
    public User create(User user) {
        checkCorrect(user);
        setNameIfNullOrBlank(user);

        String sql = "insert into users(email, login, name, birthday)" +
                " values (?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());

        sql = "select max(id) as id from users";
        Integer id = jdbcTemplate.queryForObject(sql, ((rs, rowNum) -> rs.getInt("id")));
        user.setId(id);

        log.debug("Created user {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        // проверка - существует ли такой пользователь
        findUserById(user.getId());

        checkCorrect(user);
        setNameIfNullOrBlank(user);

        String sql = "update users set email = ?, login = ?, name = ?, birthday = ? where id = ?";

        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        // удаляем друзей из БД
        sql = "delete from friend where req_from = ?";
        jdbcTemplate.update(sql,
                user.getId());

        // добавляем друзей в БД
        if (user.getFriendIds() != null) {
            Set<Integer> friendIds = user.getFriendIds();
            for (int friendId : friendIds) {
                sql = "insert into friend(req_from, req_to) values(?, ?)";
                jdbcTemplate.update(sql,
                        user.getId(),
                        friendId);
            }
        }

        log.debug("Updated user {}", user);
        return user;
    }


    private void setNameIfNullOrBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Set user name of login {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}
