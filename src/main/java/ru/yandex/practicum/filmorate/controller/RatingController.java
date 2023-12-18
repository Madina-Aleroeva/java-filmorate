package ru.yandex.practicum.filmorate.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @GetMapping
    public List<Rating> findAllRatings() {
        // TODO - filmService.findAllRatings()
        return ratingService.findAllRatings();
    }

    @GetMapping("/{id}")
    public Rating findRatingById(@PathVariable int id) {
        // TODO - filmService.findRatingById(id)
        return ratingService.findRatingById(id);
    }
}
