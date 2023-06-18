package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.impl.EntitiesForBookingTests;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
@AutoConfigureTestDatabase
class BookingRepositoryTest extends EntitiesForBookingTests {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookingRepository bookingRepository;

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
                        .item(items.get(1))
                        .status(BookingStatus.REJECTED)
                        .start(LocalDateTime.now().minusDays(1).withNano(0))
                        .end(LocalDateTime.now().plusDays(1).withNano(0))
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
    void shouldFindRequestsByBookerIdWhenUseFindByBookerId() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByBookerId(users.get(1).getId(), pageRequest);
        assertThat(findEntities, hasSize(3));
        for (Booking testBooking : bookings) {
            Item testBookingItem = testBooking.getItem();
            User testBookingBooker = testBooking.getBooker();

            assertThat(findEntities, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(testBooking.getStart())),
                    hasProperty("end", equalTo(testBooking.getEnd())),
                    hasProperty("item", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBookingItem.getName())),
                            hasProperty("description", equalTo(testBookingItem.getDescription())),
                            hasProperty("available", equalTo(testBookingItem.getAvailable()))
                    )),
                    hasProperty("booker", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBookingBooker.getName())),
                            hasProperty("email", equalTo(testBookingBooker.getEmail()))
                    )),
                    hasProperty("status", equalTo(testBooking.getStatus()))
            )));
        }
    }

    @Test
    void shouldFindRequestsByBookerIdAndStatusApprovedWhenFindByBookerIdAndStatusApproved() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByBookerIdAndStatus(users.get(1).getId(),
                BookingStatus.APPROVED, pageRequest);
        Booking testBooking = bookings.get(1);
        Item testBookingItem = testBooking.getItem();
        User testBookingBooker = testBooking.getBooker();

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingItem.getName())),
                        hasProperty("description", equalTo(testBookingItem.getDescription())),
                        hasProperty("available", equalTo(testBookingItem.getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingBooker.getName())),
                        hasProperty("email", equalTo(testBookingBooker.getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }


    @Test
    void shouldFindRequestsByBookerIdAndEndIsAfterAndStartIsBeforeWhenFindByBookerIdAndEndIsAfterAndStartIsBefore() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByBookerIdAndEndIsAfterAndStartIsBefore(users.get(1).getId(),
                LocalDateTime.now(), LocalDateTime.now(), pageRequest);
        Booking testBooking = bookings.get(2);
        Item testBookingItem = testBooking.getItem();
        User testBookingBooker = testBooking.getBooker();

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingItem.getName())),
                        hasProperty("description", equalTo(testBookingItem.getDescription())),
                        hasProperty("available", equalTo(testBookingItem.getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingBooker.getName())),
                        hasProperty("email", equalTo(testBookingBooker.getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void shouldFindRequestsByBookerIdAndStartIsAfterWhenUseFindByBookerIdAndStartIsAfter() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByBookerIdAndStartIsAfter(users.get(1).getId(),
                LocalDateTime.now(), pageRequest);
        Booking testBooking = bookings.get(1);
        Item testBookingItem = testBooking.getItem();
        User testBookingBooker = testBooking.getBooker();

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingItem.getName())),
                        hasProperty("description", equalTo(testBookingItem.getDescription())),
                        hasProperty("available", equalTo(testBookingItem.getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingBooker.getName())),
                        hasProperty("email", equalTo(testBookingBooker.getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void shouldFindRequestsByBookerIdAndEndIsBeforeWhenUseFindByBookerIdAndEndIsBefore() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByBookerIdAndEndIsBefore(users.get(1).getId(),
                LocalDateTime.now(), pageRequest);
        Booking testBooking = bookings.get(0);
        Item testBookingItem = testBooking.getItem();
        User testBookingBooker = testBooking.getBooker();

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingItem.getName())),
                        hasProperty("description", equalTo(testBookingItem.getDescription())),
                        hasProperty("available", equalTo(testBookingItem.getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingBooker.getName())),
                        hasProperty("email", equalTo(testBookingBooker.getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void shouldFindRequestsByOwnerIdWhenUseFindByOwnerId() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByItemOwnerId(users.get(0).getId(), pageRequest);

        assertThat(findEntities, hasSize(3));
        for (Booking testBooking : bookings) {
            Item testBookingItem = testBooking.getItem();
            User testBookingBooker = testBooking.getBooker();
            assertThat(findEntities, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(testBooking.getStart())),
                    hasProperty("end", equalTo(testBooking.getEnd())),
                    hasProperty("item", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBookingItem.getName())),
                            hasProperty("description", equalTo(testBookingItem.getDescription())),
                            hasProperty("available", equalTo(testBookingItem.getAvailable()))
                    )),
                    hasProperty("booker", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBookingBooker.getName())),
                            hasProperty("email", equalTo(testBookingBooker.getEmail()))
                    )),
                    hasProperty("status", equalTo(testBooking.getStatus()))
            )));
        }
    }

    @Test
    void shouldFindRequestsByOwnerIdAndStatusApprovedWhenFindByOwnerIdAndStatusApproved() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByItemOwnerIdAndStatus(users.get(0).getId(),
                BookingStatus.APPROVED, pageRequest);
        Booking testBooking = bookings.get(1);
        Item testBookingItem = testBooking.getItem();
        User testBookingBooker = testBooking.getBooker();

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingItem.getName())),
                        hasProperty("description", equalTo(testBookingItem.getDescription())),
                        hasProperty("available", equalTo(testBookingItem.getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingBooker.getName())),
                        hasProperty("email", equalTo(testBookingBooker.getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void shouldFindRequestsByOwnerIdAndEndIsAfterAndStartIsBeforeWhenFindByOwnerIdAndEndIsAfterAndStartIsBefore() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByItemOwnerIdAndEndIsAfterAndStartIsBefore(users.get(0).getId(),
                LocalDateTime.now(), LocalDateTime.now(), pageRequest);
        Booking testBooking = bookings.get(2);
        Item testBookingItem = testBooking.getItem();
        User testBookingBooker = testBooking.getBooker();

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingItem.getName())),
                        hasProperty("description", equalTo(testBookingItem.getDescription())),
                        hasProperty("available", equalTo(testBookingItem.getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingBooker.getName())),
                        hasProperty("email", equalTo(testBookingBooker.getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void shouldFindRequestsByOwnerIdAndStartIsAfterWhenUseFindByOwnerIdAndStartIsAfter() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByItemOwnerIdAndStartIsAfter(users.get(0).getId(),
                LocalDateTime.now(), pageRequest);
        Booking testBooking = bookings.get(1);
        Item testBookingItem = testBooking.getItem();
        User testBookingBooker = testBooking.getBooker();

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingItem.getName())),
                        hasProperty("description", equalTo(testBookingItem.getDescription())),
                        hasProperty("available", equalTo(testBookingItem.getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingBooker.getName())),
                        hasProperty("email", equalTo(testBookingBooker.getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void shouldFindRequestsByOwnerIdAndEndIsBeforeWhenUseFindByItemOwnerIdAndEndIsBefore() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByItemOwnerIdAndEndIsBefore(users.get(0).getId(),
                LocalDateTime.now(), pageRequest);
        Booking testBooking = bookings.get(0);
        Item testBookingItem = testBooking.getItem();
        User testBookingBooker = testBooking.getBooker();

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingItem.getName())),
                        hasProperty("description", equalTo(testBookingItem.getDescription())),
                        hasProperty("available", equalTo(testBookingItem.getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBookingBooker.getName())),
                        hasProperty("email", equalTo(testBookingBooker.getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void shouldFindByItemIdAndItemOwnerId() {
        List<Booking> findEntities = bookingRepository.findByItem_IdAndItemOwnerId(items.get(0).getId(),
                users.get(0).getId());

        assertThat(findEntities, hasSize(2));
        for (Booking testBooking : List.of(bookings.get(0), bookings.get(1))) {
            Item testBookingItem = testBooking.getItem();
            User testBookingBooker = testBooking.getBooker();

            assertThat(findEntities, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(testBooking.getStart())),
                    hasProperty("end", equalTo(testBooking.getEnd())),
                    hasProperty("item", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBookingItem.getName())),
                            hasProperty("description", equalTo(testBookingItem.getDescription())),
                            hasProperty("available", equalTo(testBookingItem.getAvailable()))
                    )),
                    hasProperty("booker", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBookingBooker.getName())),
                            hasProperty("email", equalTo(testBookingBooker.getEmail()))
                    )),
                    hasProperty("status", equalTo(testBooking.getStatus()))
            )));
        }
    }

    @Test
    void shouldFindTopByStatusNotLikeAndBookerIdAndItemId() {
        Optional<Booking> maybeFindEntity = bookingRepository
                .findTopByStatusNotLikeAndBookerIdAndItemId(BookingStatus.REJECTED, users.get(1).getId(),
                        items.get(0).getId(), Sort.by(Sort.Direction.ASC, "end"));
        Booking testBooking = bookings.get(0);
        Item testBookingItem = testBooking.getItem();
        User testBookingBooker = testBooking.getBooker();

        if (maybeFindEntity.isPresent()) {
            Booking findEntity = maybeFindEntity.get();
            assertThat(findEntity, allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(testBooking.getStart())),
                    hasProperty("end", equalTo(testBooking.getEnd())),
                    hasProperty("item", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBookingItem.getName())),
                            hasProperty("description", equalTo(testBookingItem.getDescription())),
                            hasProperty("available", equalTo(testBookingItem.getAvailable()))
                    )),
                    hasProperty("booker", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBookingBooker.getName())),
                            hasProperty("email", equalTo(testBookingBooker.getEmail()))
                    )),
                    hasProperty("status", equalTo(testBooking.getStatus()))
            ));
        }
    }
}