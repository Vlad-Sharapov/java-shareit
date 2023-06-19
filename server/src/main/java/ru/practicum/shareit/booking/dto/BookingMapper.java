package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.*;

public class BookingMapper {

    public static Booking toBooking(BookingDto bookingDto, User booker, Item item) {
        Booking booking = new Booking();

        LocalDateTime endDate = bookingDto.getEnd().withNano(0);
        LocalDateTime startDate = bookingDto.getStart().withNano(0);

        booking.setId(bookingDto.getId());
        booking.setStart(startDate);
        booking.setEnd(endDate);
        booking.setItem(item);
        booking.setBooker(booker);
        return booking;
    }

    public static BookingDto toBookingDto(Booking booking) {

        LocalDateTime start = booking.getStart().withNano(0);
        LocalDateTime end = booking.getEnd().withNano(0);

        return BookingDto.builder()
                .id(booking.getId())
                .start(start)
                .end(end)
                .itemId(booking.getItem().getId())
                .bookerId(booking.getBooker().getId())
                .status(booking.getStatus())
                .build();
    }

    public static BookingDtoOutput toBookingDtoOutput(Booking booking) {

        LocalDateTime start = booking.getStart().withNano(0);
        LocalDateTime end = booking.getEnd().withNano(0);

        return BookingDtoOutput.builder()
                .id(booking.getId())
                .start(start)
                .end(end)
                .item(ItemMapper.toItemDto(booking.getItem()))
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .status(booking.getStatus())
                .build();
    }
}
