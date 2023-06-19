package ru.practicum.shareit.booking.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.booking.enums.BookingStatus.*;


@SpringBootTest
@AutoConfigureTestDatabase
@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest extends EntitiesForBookingTests {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    private BookingService bookingService;

    private final Item item1 = items.get(0);
    private final Booking booking1 = bookings.get(0);
    private final Booking booking2 = bookings.get(1);

    @BeforeEach
    public void beforeEach() {
        bookingService = new BookingServiceImpl(itemRepository, userRepository,
                bookingRepository);
    }

    @Test
    void add() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(itemRepository.findById(item1.getId()))
                .thenReturn(Optional.of(item1));
        when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenReturn(booking1);

        BookingDtoOutput newBooking = bookingService.add(user1.getId(), BookingDto.builder()
                .itemId(booking1.getItem().getId())
                .start(booking1.getStart())
                .end(booking1.getEnd())
                .build());

        assertThat(newBooking, allOf(
                hasProperty("id", equalTo(booking1.getId())),
                hasProperty("start", equalTo(booking1.getStart())),
                hasProperty("end", equalTo(booking1.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(booking1.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(booking1.getBooker()))),
                hasProperty("status", equalTo(booking1.getStatus()))
        ));

    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenBookerIsAnOwner() {
        when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));
        when(itemRepository.findById(item1.getId()))
                .thenReturn(Optional.of(item1));

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.add(user2.getId(), BookingDto.builder()
                        .itemId(booking1.getItem().getId())
                        .start(booking1.getStart())
                        .end(booking1.getEnd())
                        .build()));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenItemIsAlreadyBooked() {
        Item bookedItem = item1.toBuilder()
                .available(false)
                .build();
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(itemRepository.findById(bookedItem.getId()))
                .thenReturn(Optional.of(bookedItem));

        assertThrows(BadRequestException.class,
                () -> bookingService.add(user1.getId(), BookingDto.builder()
                        .itemId(booking1.getItem().getId())
                        .start(booking1.getStart())
                        .end(booking1.getEnd())
                        .build()));
    }

    @Test
    void shouldUpdatingBookingOnApproved() {
        Booking approvedBooking1 = booking1.toBuilder().status(APPROVED).build();
        when(bookingRepository.findById(booking1.getId()))
                .thenReturn(Optional.of(booking1));
        when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenReturn(approvedBooking1);

        BookingDtoOutput approved = bookingService.approved(user2.getId(), booking1.getId(), true);

        assertThat(approved, allOf(
                hasProperty("id", equalTo(approvedBooking1.getId())),
                hasProperty("start", equalTo(approvedBooking1.getStart())),
                hasProperty("end", equalTo(approvedBooking1.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(approvedBooking1.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(booking1.getBooker()))),
                hasProperty("status", equalTo(approvedBooking1.getStatus()))
        ));
    }

    @Test
    void shouldUpdatingBookingOnNotApproved() {
        Booking rejectedBooking1 = booking1.toBuilder().status(REJECTED).build();
        when(bookingRepository.findById(booking1.getId()))
                .thenReturn(Optional.of(booking1));
        when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenReturn(rejectedBooking1);

        BookingDtoOutput approved = bookingService.approved(user2.getId(), booking1.getId(), false);

        assertThat(approved, allOf(
                hasProperty("id", equalTo(rejectedBooking1.getId())),
                hasProperty("start", equalTo(rejectedBooking1.getStart())),
                hasProperty("end", equalTo(rejectedBooking1.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(rejectedBooking1.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(booking1.getBooker()))),
                hasProperty("status", equalTo(rejectedBooking1.getStatus()))
        ));
    }

    @Test
    void shouldEntityNotFoundExceptionWhenUserPermissionDenied() {
        when(bookingRepository.findById(booking1.getId()))
                .thenReturn(Optional.of(booking1));

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.approved(user1.getId(), booking1.getId(), true));
    }

    @Test
    void shouldEntityNotFoundExceptionWhenBookingAlreadyIsApproved() {
        Booking approvedBooking1 = booking1.toBuilder().status(APPROVED).build();
        when(bookingRepository.findById(booking1.getId()))
                .thenReturn(Optional.of(approvedBooking1));

        assertThrows(BadRequestException.class,
                () -> bookingService.approved(user2.getId(), booking1.getId(), true));
    }

    @Test
    void shouldEntityNotFoundExceptionWhenBookingAlreadyIsRejected() {
        Booking rejectedBooking1 = booking1.toBuilder().status(REJECTED).build();
        when(bookingRepository.findById(booking1.getId()))
                .thenReturn(Optional.of(rejectedBooking1));

        assertThrows(BadRequestException.class,
                () -> bookingService.approved(user2.getId(), booking1.getId(), false));
    }


    @Test
    void shouldGetBookingForBooker() {
        when(bookingRepository.findById(booking1.getId()))
                .thenReturn(Optional.of(booking1));

        BookingDtoOutput bookingDto = bookingService.getBooking(user1.getId(), booking1.getId());

        assertThat(bookingDto, allOf(
                hasProperty("id", equalTo(booking1.getId())),
                hasProperty("start", equalTo(booking1.getStart())),
                hasProperty("end", equalTo(booking1.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(booking1.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(booking1.getBooker()))),
                hasProperty("status", equalTo(booking1.getStatus()))
        ));
    }

    @Test
    void shouldGetBookingForOwner() {
        when(bookingRepository.findById(booking1.getId()))
                .thenReturn(Optional.of(booking1));

        BookingDtoOutput bookingDto = bookingService.getBooking(user2.getId(), booking1.getId());

        assertThat(bookingDto, allOf(
                hasProperty("id", equalTo(booking1.getId())),
                hasProperty("start", equalTo(booking1.getStart())),
                hasProperty("end", equalTo(booking1.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(booking1.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(booking1.getBooker()))),
                hasProperty("status", equalTo(booking1.getStatus()))
        ));
    }

    @Test
    void shouldGetBookingForOtherUser() {
        when(bookingRepository.findById(booking1.getId()))
                .thenReturn(Optional.of(booking1));

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getBooking(3L, booking1.getId()));

    }

    @Test
    void getAllUserBookingsWithStatusAll() {
        List<Booking> testBookings = List.of(booking1, booking2);

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByBookerId(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(testBookings);

        List<BookingDtoOutput> all = bookingService.getAllUserBookings(user1.getId(), "ALL", 0, 5);

        for (Booking testBooking : testBookings) {
            assertThat(all, hasItem(allOf(
                    hasProperty("id", equalTo(testBooking.getId())),
                    hasProperty("start", equalTo(testBooking.getStart())),
                    hasProperty("end", equalTo(testBooking.getEnd())),
                    hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                    hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                    hasProperty("status", equalTo(testBooking.getStatus()))
            )));
        }
    }

    @Test
    void getAllUserBookingsWithStatusWaiting() {
        List<Booking> testBookings = List.of(booking1);

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByBookerIdAndStatus(Mockito.anyLong(),
                Mockito.any(BookingStatus.class),
                Mockito.any(Pageable.class)))
                .thenReturn(testBookings);

        List<BookingDtoOutput> all = bookingService.getAllUserBookings(user1.getId(), "WAITING", 0, 5);

        assertThat(all, hasSize(1));
        assertThat(all, hasItem(allOf(
                hasProperty("id", equalTo(booking1.getId())),
                hasProperty("start", equalTo(booking1.getStart())),
                hasProperty("end", equalTo(booking1.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(booking1.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(booking1.getBooker()))),
                hasProperty("status", equalTo(booking1.getStatus()))
        )));
    }

    @Test
    void shouldExceptionWhenUseGetAllUserBookingsUserNotFound() {

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getAllUserBookings(user1.getId(), "WAITING", 0, 5));
    }

    @Test
    void getAllUserBookingsWithStatusRejected() {
        List<Booking> testBookings = List.of(booking1.toBuilder().status(REJECTED).build());

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByBookerIdAndStatus(Mockito.anyLong(),
                Mockito.any(BookingStatus.class),
                Mockito.any(Pageable.class)))
                .thenReturn(testBookings);

        List<BookingDtoOutput> all = bookingService.getAllUserBookings(user1.getId(), "REJECTED", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = testBookings.get(0);
        assertThat(all, hasItem(allOf(
                hasProperty("id", equalTo(testBooking.getId())),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        )));
    }

    @Test
    void getAllUserBookingsWithStatusCurrent() {
        List<Booking> testBookings = List.of(booking1.toBuilder()
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1)).build());

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByBookerIdAndEndIsAfterAndStartIsBefore(Mockito.anyLong(),
                Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)))
                .thenReturn(testBookings);

        List<BookingDtoOutput> all = bookingService.getAllUserBookings(user1.getId(), "CURRENT", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = testBookings.get(0);
        assertThat(all, hasItem(allOf(
                hasProperty("id", equalTo(testBooking.getId())),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        )));
    }

    @Test
    void getAllUserBookingsWithStatusPast() {
        List<Booking> testBookings = List.of(booking2);

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByBookerIdAndEndIsBefore(Mockito.anyLong(),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)))
                .thenReturn(testBookings);

        List<BookingDtoOutput> all = bookingService.getAllUserBookings(user1.getId(), "PAST", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = testBookings.get(0);
        assertThat(all, hasItem(allOf(
                hasProperty("id", equalTo(testBooking.getId())),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        )));
    }

    @Test
    void getAllUserBookingsWithStatusFuture() {
        List<Booking> testBookings = List.of(booking1);

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByBookerIdAndStartIsAfter(Mockito.anyLong(),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)))
                .thenReturn(testBookings);

        List<BookingDtoOutput> all = bookingService.getAllUserBookings(user1.getId(), "FUTURE", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = testBookings.get(0);
        assertThat(all, hasItem(allOf(
                hasProperty("id", equalTo(testBooking.getId())),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        )));
    }

    @Test
    void getAllOwnerBookings() {
        List<Booking> testBookings = List.of(booking1, booking2);

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByItemOwnerId(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(testBookings);

        List<BookingDtoOutput> all = bookingService.getAllOwnerBookings(user1.getId(), "ALL", 0, 5);

        for (Booking testBooking : testBookings) {
            assertThat(all, hasItem(allOf(
                    hasProperty("id", equalTo(testBooking.getId())),
                    hasProperty("start", equalTo(testBooking.getStart())),
                    hasProperty("end", equalTo(testBooking.getEnd())),
                    hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                    hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                    hasProperty("status", equalTo(testBooking.getStatus()))
            )));
        }

    }

    @Test
    void getAllOwnerBookingsWithStatusWaiting() {
        List<Booking> testBookings = List.of(booking1);

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByItemOwnerIdAndStatus(Mockito.anyLong(),
                Mockito.any(BookingStatus.class),
                Mockito.any(Pageable.class)))
                .thenReturn(testBookings);

        List<BookingDtoOutput> all = bookingService.getAllOwnerBookings(user1.getId(), "WAITING", 0, 5);

        assertThat(all, hasSize(1));
        assertThat(all, hasItem(allOf(
                hasProperty("id", equalTo(booking1.getId())),
                hasProperty("start", equalTo(booking1.getStart())),
                hasProperty("end", equalTo(booking1.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(booking1.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(booking1.getBooker()))),
                hasProperty("status", equalTo(booking1.getStatus()))
        )));
    }

    @Test
    void shouldExceptionWhenUseGetAllOwnerBookingsUserNotFound() {

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getAllOwnerBookings(user1.getId(), "WAITING", 0, 5));
    }

    @Test
    void getAllOwnerBookingsWithStatusRejected() {
        List<Booking> testBookings = List.of(booking1.toBuilder().status(REJECTED).build());

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByItemOwnerIdAndStatus(Mockito.anyLong(),
                Mockito.any(BookingStatus.class),
                Mockito.any(Pageable.class)))
                .thenReturn(testBookings);

        List<BookingDtoOutput> all = bookingService.getAllOwnerBookings(user1.getId(), "REJECTED", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = testBookings.get(0);
        assertThat(all, hasItem(allOf(
                hasProperty("id", equalTo(testBooking.getId())),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        )));
    }

    @Test
    void getAllOwnerBookingsWithStatusCurrent() {
        List<Booking> testBookings = List.of(booking1.toBuilder()
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1)).build());

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByItemOwnerIdAndEndIsAfterAndStartIsBefore(Mockito.anyLong(),
                Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)))
                .thenReturn(testBookings);

        List<BookingDtoOutput> all = bookingService.getAllOwnerBookings(user1.getId(), "CURRENT", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = testBookings.get(0);
        assertThat(all, hasItem(allOf(
                hasProperty("id", equalTo(testBooking.getId())),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        )));
    }

    @Test
    void getAllOwnerBookingsWithStatusPast() {
        List<Booking> testBookings = List.of(booking2);

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByItemOwnerIdAndEndIsBefore(Mockito.anyLong(),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)))
                .thenReturn(testBookings);

        List<BookingDtoOutput> all = bookingService.getAllOwnerBookings(user1.getId(), "PAST", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = testBookings.get(0);
        assertThat(all, hasItem(allOf(
                hasProperty("id", equalTo(testBooking.getId())),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        )));
    }

    @Test
    void getAllOwnerBookingsWithStatusFuture() {
        List<Booking> testBookings = List.of(booking1);

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(bookingRepository.findByItemOwnerIdAndStartIsAfter(Mockito.anyLong(),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)))
                .thenReturn(testBookings);

        List<BookingDtoOutput> all = bookingService.getAllOwnerBookings(user1.getId(), "FUTURE", 0, 5);

        assertThat(all, hasSize(1));
        Booking testBooking = testBookings.get(0);
        assertThat(all, hasItem(allOf(
                hasProperty("id", equalTo(testBooking.getId())),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", equalTo(ItemMapper.toItemDto(testBooking.getItem()))),
                hasProperty("booker", equalTo(UserMapper.toUserDto(testBooking.getBooker()))),
                hasProperty("status", equalTo(testBooking.getStatus()))
        )));
    }

}