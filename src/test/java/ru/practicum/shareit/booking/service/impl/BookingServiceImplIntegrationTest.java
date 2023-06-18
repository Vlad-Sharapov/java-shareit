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
import ru.practicum.shareit.user.dto.UserMapper;

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

        users.forEach(em::persist);

        em.persist(itemRequest);

        items.forEach(em::persist);

        bookings.forEach(em::persist);

        em.persist(comment.toBuilder()
                .id(null)
                .author(users.get(1))
                .item(items.get(0))
                .build());
        em.flush();
    }

    @Test
    void shouldGetAllUserBookingsWhenUseGetAllUserBookings() {

        List<BookingDtoOutput> all = getAllUserBookings(BookingStatus.ALL);

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
    void shouldGetRejectedUserBookingsWhenUseGetAllUserBookingsWithFilterRejected() {

        List<BookingDtoOutput> all = getAllUserBookings(BookingStatus.REJECTED);

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
    void shouldGetCurrentlyUserBookingsWhenUseGetAllUserBookingsWithFilterCurrent() {

        List<BookingDtoOutput> all = getAllUserBookings(BookingStatus.CURRENT);

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
    void shouldGetWaitingUserBookingsWhenUseGetAllUserBookingsWithFilterWaiting() {

        List<BookingDtoOutput> all = getAllUserBookings(BookingStatus.WAITING);

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
    void shouldGetPastUserBookingsWhenUseGetAllOwnerBookingsWithFilterPast() {

        List<BookingDtoOutput> all = getAllUserBookings(BookingStatus.PAST);

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
    void shouldGetFutureUserBookingsWhenUseGetAllOwnerBookingsWithFilterFuture() {

        List<BookingDtoOutput> all = getAllUserBookings(BookingStatus.FUTURE);

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
    void shouldGetAllOwnerBookingsWhenUseGetAllOwnerBookings() {

        List<BookingDtoOutput> all = getAllOwnerBookings(BookingStatus.ALL);

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
    void shouldGetRejectedOwnerBookingsWhenUseGetAllOwnerBookingsWithFilterRejected() {

        List<BookingDtoOutput> all = getAllOwnerBookings(BookingStatus.REJECTED);

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
    void shouldGetCurrentlyOwnerBookingsWhenUseGetAllOwnerBookingsWithFilterCurrent() {

        List<BookingDtoOutput> all = getAllOwnerBookings(BookingStatus.CURRENT);

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
    void shouldGetWaitingOwnerBookingsWhenUseGetAllOwnerBookingsWithFilterWaiting() {

        List<BookingDtoOutput> all = getAllOwnerBookings(BookingStatus.WAITING);

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
    void shouldGetPastOwnerBookingsWhenUseGetAllOwnerBookingsWithFilterPast() {

        List<BookingDtoOutput> all = getAllOwnerBookings(BookingStatus.PAST);

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
    void shouldGetFutureOwnerBookingsWhenUseGetAllOwnerBookingsWithFilterFuture() {

        List<BookingDtoOutput> all = getAllOwnerBookings(BookingStatus.FUTURE);

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

    private List<BookingDtoOutput> getAllUserBookings(BookingStatus status) {
        return bookingService.getAllUserBookings(users.get(1).getId(), status.name(), 0, 5);
    }

    private List<BookingDtoOutput> getAllOwnerBookings(BookingStatus status) {
        return bookingService.getAllOwnerBookings(users.get(0).getId(), status.name(), 0, 5);
    }
}