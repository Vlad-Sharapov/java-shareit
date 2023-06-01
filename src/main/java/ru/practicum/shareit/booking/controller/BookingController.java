package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;


/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @PostMapping
    public BookingDtoOutput add(@RequestHeader(USER_ID_HEADER) Long userId,
                                @Valid @RequestBody BookingDto bookingDto) {
        return bookingService.add(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoOutput approved(@RequestHeader(USER_ID_HEADER) Long userId,
                                     @PathVariable Long bookingId,
                                     @RequestParam Boolean approved) {
        return bookingService.approved(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoOutput get(@RequestHeader(USER_ID_HEADER) Long userId,
                                @PathVariable Long bookingId) {
        return bookingService.get(userId, bookingId);
    }

    @GetMapping
    public List<BookingDtoOutput> allFromUser(@RequestHeader(USER_ID_HEADER) Long userId,
                                              @RequestParam(required = false, defaultValue = "ALL") String state) {
        return bookingService.getAllFromUser(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDtoOutput> allFromOwner(@RequestHeader(USER_ID_HEADER) Long userId,
                                               @RequestParam(required = false, defaultValue = "ALL") String state) {
        return bookingService.getAllFromOwner(userId, state);
    }
}
