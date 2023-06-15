package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;


@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplIntegrationTest extends EntitiesForBookingTests {

    private final BookingService bookingService;

    private final EntityManager em;

    @BeforeEach
    public void beforeEach() {
        users = List.of(
                users.get(1).toBuilder()
                        .id(null)
                        .build(),

                users.get(0).toBuilder()
                        .id(null)
                        .build()
        );

        itemRequest = itemRequestUser1.toBuilder()
                .id(null)
                .requestor(users.get(1))
                .build();

        items = List.of(
                items.get(0).toBuilder()
                        .id(null)
                        .request(itemRequest)
                        .owner(users.get(0))
                        .build(),
                items.get(1).toBuilder()
                        .id(null)
                        .request(null)
                        .owner(users.get(0))
                        .build()
        );

        bookings = List.of(
                bookings.get(0).toBuilder()
                        .id(null)
                        .booker(users.get(1))
                        .item(items.get(0))
                        .build(),
                bookings.get(1).toBuilder()
                        .id(null)
                        .booker(users.get(1))
                        .item(items.get(0))
                        .build(),
                bookings.get(1).toBuilder()
                        .id(null)
                        .booker(users.get(1))
                        .item(items.get(0))
                        .status(BookingStatus.REJECTED)
                        .start(LocalDateTime.now().minusDays(1))
                        .end(LocalDateTime.now().plusDays(1))
                        .build()
        );

        for (User user : users) {
            em.persist(user);
        }

        em.persist(itemRequest);

        for (Item item : items) {
            em.persist(item);
        }

        for (Booking booking : bookings) {
            em.persist(booking);
        }

        em.persist(comment.toBuilder()
                .id(null)
                .author(users.get(1))
                .item(items.get(0))
                .build());
        em.flush();
    }

    @Test
    void getAllUserBookings() {

        List<BookingDtoOutput> all = bookingService.getAllUserBookings(users.get(1).getId(), "ALL", 0, 5);

        for (Booking testBooking : bookings) {
            assertThat(all, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(testBooking.getStart())),
                    hasProperty("end", equalTo(testBooking.getEnd())),
                    hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                    hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                    hasProperty("status", equalTo(testBooking.getStatus()))
            )));
        }
    }

    @Test
    void getRejectedUserBookings() {

        List<BookingDtoOutput> all = bookingService.getAllUserBookings(users.get(1).getId(), "REJECTED", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = bookings.get(2);
        assertThat(all.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void getCurrentlyUserBookings() {

        List<BookingDtoOutput> all = bookingService.getAllUserBookings(users.get(1).getId(), "CURRENT", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = bookings.get(2);
        assertThat(all.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void getWaitingUserBookings() {

        List<BookingDtoOutput> all = bookingService.getAllUserBookings(users.get(1).getId(), "WAITING", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = bookings.get(0);
        assertThat(all.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void getPastUserBookings() {

        List<BookingDtoOutput> all = bookingService.getAllUserBookings(users.get(1).getId(), "PAST", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = bookings.get(0);
        assertThat(all.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void getFutureUserBookings() {

        List<BookingDtoOutput> all = bookingService.getAllUserBookings(users.get(1).getId(), "FUTURE", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = bookings.get(1);
        assertThat(all.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void getAllOwnerBookings() {

        List<BookingDtoOutput> all = bookingService.getAllOwnerBookings(users.get(0).getId(), "ALL", 0, 5);

        for (Booking testBooking : bookings) {
            assertThat(all, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(testBooking.getStart())),
                    hasProperty("end", equalTo(testBooking.getEnd())),
                    hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                    hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                    hasProperty("status", equalTo(testBooking.getStatus()))
            )));
        }
    }

    @Test
    void getRejectedOwnerBookings() {

        List<BookingDtoOutput> all = bookingService.getAllOwnerBookings(users.get(0).getId(), "REJECTED", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = bookings.get(2);
        assertThat(all.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void getCurrentlyOwnerBookings() {

        List<BookingDtoOutput> all = bookingService.getAllOwnerBookings(users.get(0).getId(), "CURRENT", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = bookings.get(2);
        assertThat(all.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void getWaitingOwnerBookings() {

        List<BookingDtoOutput> all = bookingService.getAllOwnerBookings(users.get(0).getId(), "WAITING", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = bookings.get(0);
        assertThat(all.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void getPastOwnerBookings() {

        List<BookingDtoOutput> all = bookingService.getAllOwnerBookings(users.get(0).getId(), "PAST", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = bookings.get(0);
        assertThat(all.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void getFutureOwnerBookings() {

        List<BookingDtoOutput> all = bookingService.getAllOwnerBookings(users.get(0).getId(), "FUTURE", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = bookings.get(1);
        assertThat(all.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }
}