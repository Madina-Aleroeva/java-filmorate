create table if not exists rating
(
    id   int primary key auto_increment,
    name varchar(8)
);

create table if not exists genre
(
    id   int primary key auto_increment,
    name varchar(64)
);

create table if not exists film
(
    id           int primary key auto_increment,
    name         varchar(100),
    description  varchar(300),
    release_date date,
    duration     float,
    rating_id    int references rating (id) on delete cascade
);

create table if not exists film_genre
(
    id       int primary key auto_increment,
    film_id  int references film (id) on delete cascade,
    genre_id int references genre (id) on delete cascade
);

create table if not exists users
(
    id       int primary key auto_increment,
    email    varchar(100),
    login    varchar(100),
    name     varchar(100),
    birthday date
);

create table if not exists friend
(
    id        int primary key auto_increment,
    req_from  int references users (id) on delete cascade,
    req_to    int references users (id) on delete cascade
);

create table if not exists film_like
(
    id      int primary key auto_increment,
    film_id int references film (id) on delete cascade,
    user_id int references users (id) on delete cascade
)










