package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;

import java.util.List;

public interface BookingService {


    BookingDtoOutput add(Long userId, BookingDto bookingDto);

    BookingDtoOutput approved(Long userId, Long bookingId, Boolean approved);

    BookingDtoOutput get(Long userId, Long bookingId);

    List<BookingDtoOutput> getAllFromUser(Long userId, String status);

    List<BookingDtoOutput> getAllFromOwner(Long userId, String status);


}
