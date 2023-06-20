package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;

import java.util.List;

public interface BookingService {


    BookingDtoOutput add(Long userId, BookingDto bookingDto);

    BookingDtoOutput approved(Long userId, Long bookingId, Boolean approved);

    BookingDtoOutput getBooking(Long userId, Long bookingId);

    List<BookingDtoOutput> getAllUserBookings(Long userId, String status, int from, int size);

    List<BookingDtoOutput> getAllOwnerBookings(Long userId, String status, int from, int size);


}
