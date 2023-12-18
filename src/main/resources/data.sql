/*truncate table GENRE;
truncate table RATING;
truncate table FILM;
truncate table FILM_GENRE;
truncate table USERS;
truncate table FRIEND;*/



insert into GENRE (name)
values ('Комедия'),
       ('Драма'),
       ('Мультфильм'),
       ('Триллер'),
       ('Документальный'),
       ('Боевик');

insert into RATING (NAME)
values ('G'),
       ('PG'),
       ('PG-13'),
       ('R'),
       ('NC-17');

/*
insert into FILM (NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
values ('Dark Knight', 'Amazing film', '2008-07-18', 152, 1),
       ('Playtime', 'Excellent film', '1967-10-05', 126, 2),
       ('Notorious', 'Nice film', '1946-02-12', 98, 2);

insert into FILM_GENRE(film_id, genre_id)
values (1, 2),
       (1, 3),
       (2, 4);

insert into USERS(email, login, name, birthday)
values ('steve@gmail.com', 'real_steve', 'steve', '1990-12-12'),
       ('mike@mail.ru', 'mike-mike', 'mike', '1980-01-29'),
       ('john@ya.ru', 'johny', 'john', '2000-05-10');

insert into FRIEND(req_from, req_to, confirmed)
values (1, 2, true),
       (2, 3, false);

insert into FILM_LIKE(film_id, user_id)
values (1, 1), (1, 2), (2, 3), (2, 1);

select * from users inner join friend on users.id = friend.req_from;
*/
