package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.validation.FilmValidation.checkCorrect;

@Component
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> findAll() {
        String sql = "select * from film join rating on film.rating_id = rating.id ";
        Map<Integer, Film> films = jdbcTemplate.query(sql, this::mapRowToFilm)
                .stream().collect(Collectors.toMap(Film::getId, Function.identity()));

        setGenresAndLikesToFilms(films, "");

        return new ArrayList<>(films.values());
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getFloat("duration"))
                .likes(new HashSet<>())
                .genres(new TreeSet<>())
                .rating(new Rating(rs.getInt("rating.id"),
                        rs.getString("rating.name")))
                .build();
    }

    private void setGenresAndLikesToFilms(Map<Integer, Film> films, String cond) {
        String sql = "select * from film " +
                "join film_genre on film_genre.film_id = film.id " +
                "join genre on genre.id = film_genre.genre_id " + cond;
        jdbcTemplate.query(sql, rs -> {
            setGenreToFilm(rs, films);
        });

        sql = "select * from film " +
                "join film_like on film_like.film_id = film.id ";
        jdbcTemplate.query(sql, rs -> {
            setLikeToFilm(rs, films);
        });
    }

    private void setGenreToFilm(ResultSet rs, Map<Integer, Film> films) throws SQLException {
        int genreId = rs.getInt("genre.id");
        String name = rs.getString("genre.name");
        int filmId = rs.getInt("film.id");
        films.get(filmId).getGenres().add(new Genre(genreId, name));
    }

    private void setLikeToFilm(ResultSet rs, Map<Integer, Film> films) throws SQLException {
        int filmId = rs.getInt("film.id");
        int userId = rs.getInt("film_like.user_id");
        films.get(filmId).getLikes().add(userId);
    }

    @Override
    public Film findFilmById(int filmId) {
        String sql = "select * from film " +
                "join rating on film.rating_id = rating.id where film.id = ?";

        Map<Integer, Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, filmId)
                .stream().collect(Collectors.toMap(Film::getId, Function.identity()));
        try {
            jdbcTemplate.queryForObject(sql, this::mapRowToFilm, filmId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Film with id = " + filmId + " not found");
        }


        if (films.isEmpty()) {
            throw new NotFoundException("Film with id = " + filmId + " not found");
        }

        setGenresAndLikesToFilms(films, "where film.id = " + filmId);

        log.debug("Found film - {}", films.get(filmId));
        return films.get(filmId);
    }

    @Override
    public Film create(Film film) {
        checkCorrect(film);

        String sql = "insert into film(name, description, release_date, " +
                "duration, rating_id) values (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating().getId());

        sql = "select max(id) as id from film";
        Integer id = jdbcTemplate.queryForObject(sql, ((rs, rowNum) -> rs.getInt("id")));
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                sql = "insert into film_genre (film_id, genre_id) values (?, ?)";
                jdbcTemplate.update(sql, id, genre.getId());
            }
        }

        log.debug("Created film - {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        findFilmById(film.getId());

        checkCorrect(film);

        String sql = "update film set " +
                "name = ?, description = ?, release_date = ?, " +
                "duration = ?, rating_id = ? where id = ?";

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating().getId(),
                film.getId());

        // удаляем лайки из БД
        sql = "delete from film_like where film_id = ?";
        jdbcTemplate.update(sql,
                film.getId());

        // добавляем лайки в БД
        if (film.getLikes() != null) {
            Set<Integer> filmLikes = film.getLikes();
            for (int userId : filmLikes) {
                sql = "insert into film_like(film_id, user_id) values(?, ?)";
                jdbcTemplate.update(sql, film.getId(), userId);
            }
        }

        // удаляем жанры из БД
        sql = "delete from film_genre where film_id = ?";
        jdbcTemplate.update(sql,
                film.getId());

        // добавляем жанры в БД
        if (film.getGenres() != null) {
            Set<Genre> filmGenres = film.getGenres();
            for (Genre genre : filmGenres) {
                sql = "insert into film_genre(film_id, genre_id) values(?, ?)";
                jdbcTemplate.update(sql, film.getId(), genre.getId());
            }
        }

        if (film.getGenres() != null) {
            film.setGenres(film.getGenres().stream().sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
        return film;
    }

    public List<Genre> findAllGenres() {
        String sql = "select * from genre";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    public Genre findGenreById(int id) {
        String sql = "select * from genre where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToGenre, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Genre with id = " + id + " not found");
        }
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("id"), rs.getString("name"));
    }

    public List<Rating> findAllRatings() {
        String sql = "select * from rating";
        return jdbcTemplate.query(sql, this::mapRowToRating);
    }

    public Rating findRatingById(int id) {
        String sql = "select * from rating where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToRating, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Rating with id = " + id + " not found");
        }
    }

    private Rating mapRowToRating(ResultSet rs, int rowNum) throws SQLException {
        return new Rating(rs.getInt("id"), rs.getString("name"));
    }


}
