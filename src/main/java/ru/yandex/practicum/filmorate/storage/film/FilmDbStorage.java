package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        // В данном случае разделяем запросы, 1) соединяем фильм и рейтинг 1 к 1,
        // 2) соединяем фильм и жанры (т.к. их несколько), 3) точно также соединяем фильмы и лайки
        // В случае соединения всех 4-х таблиц сразу - получим слишком большую таблицу
        String sql = "select * from film inner join rating on film.rating_id = rating.id ";
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
                "inner join film_genre on film_genre.film_id = film.id " +
                "inner join genre on genre.id = film_genre.genre_id " + cond;
        jdbcTemplate.query(sql, (rs, rowNum) -> setGenreToFilm(rs, films));

        sql = "select * from film " +
                "inner join film_like on film_like.film_id = film.id ";
        jdbcTemplate.query(sql, (rs, rowNum) -> setLikeToFilm(rs, films));

    }

    private Object setGenreToFilm(ResultSet rs, Map<Integer, Film> films) throws SQLException {
        log.debug("setGenreToFilm begin");
        int genreId = rs.getInt("genre.id");
        String name = rs.getString("genre.name");
        int filmId = rs.getInt("film.id");
        films.get(filmId).getGenres().add(new Genre(genreId, name));

        return null;
    }

    private Object setLikeToFilm(ResultSet rs, Map<Integer, Film> films) throws SQLException {
        int filmId = rs.getInt("film.id");
        int userId = rs.getInt("film_like.user_id");
        films.get(filmId).getLikes().add(userId);

        return null;
    }

    @Override
    public Film findFilmById(int filmId) {
        String sql = "select * from film inner join rating " +
                "on film.rating_id = rating.id where film.id = ?";
        Map<Integer, Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, filmId)
                .stream().collect(Collectors.toMap(Film::getId, Function.identity()));

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
            log.debug("in film with id = {} are genres {}", id, film.getGenres());
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
        sql = "select * from film inner join film_like on film.id = film_like.film_id where film.id = ?";
        jdbcTemplate.query(sql, (rs, rowNum) -> checkDeleteLikes(rs, film), film.getId());

        // добавляем лайки в БД
        if (film.getLikes() != null) {
            Set<Integer> filmLikes = film.getLikes();
            for (int userId : filmLikes) {
                sql = "insert into film_like(film_id, user_id) values(?, ?)";
                jdbcTemplate.update(sql, film.getId(), userId);
            }
        }

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

        log.debug("Updated film {}", film);
        if (film.getGenres() != null) {
            film.setGenres(film.getGenres().stream().sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
        return film;
    }

    private Object checkDeleteLikes(ResultSet rs, Film film) throws SQLException {
        int userId = rs.getInt("film_like.user_id");
        if (!film.getLikes().contains(userId)) {
            String sql = "delete from film_like where film_id = ? and user_id = ?";
            jdbcTemplate.update(sql,
                    film.getId(),
                    userId);
        }

        return null;
    }

    private Object checkDeleteGenres(ResultSet rs, Film film) throws SQLException {
        int genreId = rs.getInt("film_genre.genre_id");
        if (film.getGenres().stream().map(genre -> genre.getId() == genreId).findAny().isEmpty()) {
            String sql = "delete from film_genre where film_id = ?";
            jdbcTemplate.update(sql,
                    film.getId(),
                    genreId);
        }

        return null;
    }

    public List<Genre> findAllGenres() {
        String sql = "select * from genre";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    public Genre findGenreById(int id) {
        String sql = "select * from genre where id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre, id);
        if (genres.isEmpty()) {
            throw new NotFoundException("Genre with id = " + id + " not found");
        }
        return genres.get(0);
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
        List<Rating> ratings = jdbcTemplate.query(sql, this::mapRowToRating, id);
        if (ratings.isEmpty()) {
            throw new NotFoundException("Rating with id = " + id + " not found");
        }
        return ratings.get(0);
    }

    private Rating mapRowToRating(ResultSet rs, int rowNum) throws SQLException {
        return new Rating(rs.getInt("id"), rs.getString("name"));
    }


}
