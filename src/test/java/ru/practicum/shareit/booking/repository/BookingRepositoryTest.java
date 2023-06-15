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
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;
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
    void findByBookerId() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByBookerId(users.get(1).getId(), pageRequest);

        assertThat(findEntities, hasSize(3));
        for (Booking testBooking : bookings) {
            assertThat(findEntities, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(testBooking.getStart())),
                    hasProperty("end", equalTo(testBooking.getEnd())),
                    hasProperty("item", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBooking.getItem().getName())),
                            hasProperty("description", equalTo(testBooking.getItem().getDescription())),
                            hasProperty("available", equalTo(testBooking.getItem().getAvailable()))
                    )),
                    hasProperty("booker", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBooking.getBooker().getName())),
                            hasProperty("email", equalTo(testBooking.getBooker().getEmail()))
                    )),
                    hasProperty("status", equalTo(testBooking.getStatus()))
            )));
        }
    }

    @Test
    void findByBookerIdAndStatusApproved() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByBookerIdAndStatus(users.get(1).getId(),
                BookingStatus.APPROVED, pageRequest);
        Booking testBooking = bookings.get(1);

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getItem().getName())),
                        hasProperty("description", equalTo(testBooking.getItem().getDescription())),
                        hasProperty("available", equalTo(testBooking.getItem().getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getBooker().getName())),
                        hasProperty("email", equalTo(testBooking.getBooker().getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }


    @Test
    void findByBookerIdAndEndIsAfterAndStartIsBefore() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByBookerIdAndEndIsAfterAndStartIsBefore(users.get(1).getId(),
                LocalDateTime.now(), LocalDateTime.now(), pageRequest);
        Booking testBooking = bookings.get(2);

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getItem().getName())),
                        hasProperty("description", equalTo(testBooking.getItem().getDescription())),
                        hasProperty("available", equalTo(testBooking.getItem().getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getBooker().getName())),
                        hasProperty("email", equalTo(testBooking.getBooker().getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void findByBookerIdAndStartIsAfter() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByBookerIdAndStartIsAfter(users.get(1).getId(),
                LocalDateTime.now(), pageRequest);
        Booking testBooking = bookings.get(1);

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getItem().getName())),
                        hasProperty("description", equalTo(testBooking.getItem().getDescription())),
                        hasProperty("available", equalTo(testBooking.getItem().getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getBooker().getName())),
                        hasProperty("email", equalTo(testBooking.getBooker().getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void findByBookerIdAndEndIsBefore() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByBookerIdAndEndIsBefore(users.get(1).getId(),
                LocalDateTime.now(), pageRequest);
        Booking testBooking = bookings.get(0);

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getItem().getName())),
                        hasProperty("description", equalTo(testBooking.getItem().getDescription())),
                        hasProperty("available", equalTo(testBooking.getItem().getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getBooker().getName())),
                        hasProperty("email", equalTo(testBooking.getBooker().getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void findByItemOwnerId() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByItemOwnerId(users.get(0).getId(), pageRequest);

        assertThat(findEntities, hasSize(3));
        for (Booking testBooking : bookings) {
            assertThat(findEntities, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(testBooking.getStart())),
                    hasProperty("end", equalTo(testBooking.getEnd())),
                    hasProperty("item", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBooking.getItem().getName())),
                            hasProperty("description", equalTo(testBooking.getItem().getDescription())),
                            hasProperty("available", equalTo(testBooking.getItem().getAvailable()))
                    )),
                    hasProperty("booker", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBooking.getBooker().getName())),
                            hasProperty("email", equalTo(testBooking.getBooker().getEmail()))
                    )),
                    hasProperty("status", equalTo(testBooking.getStatus()))
            )));
        }
    }

    @Test
    void findByItemOwnerIdAndStatus() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByItemOwnerIdAndStatus(users.get(0).getId(),
                BookingStatus.APPROVED, pageRequest);
        Booking testBooking = bookings.get(1);

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getItem().getName())),
                        hasProperty("description", equalTo(testBooking.getItem().getDescription())),
                        hasProperty("available", equalTo(testBooking.getItem().getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getBooker().getName())),
                        hasProperty("email", equalTo(testBooking.getBooker().getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void findByItemOwnerIdAndEndIsAfterAndStartIsBefore() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByItemOwnerIdAndEndIsAfterAndStartIsBefore(users.get(0).getId(),
                LocalDateTime.now(), LocalDateTime.now(), pageRequest);
        Booking testBooking = bookings.get(2);

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getItem().getName())),
                        hasProperty("description", equalTo(testBooking.getItem().getDescription())),
                        hasProperty("available", equalTo(testBooking.getItem().getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getBooker().getName())),
                        hasProperty("email", equalTo(testBooking.getBooker().getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void findByItemOwnerIdAndStartIsAfter() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByItemOwnerIdAndStartIsAfter(users.get(0).getId(),
                LocalDateTime.now(), pageRequest);
        Booking testBooking = bookings.get(1);

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getItem().getName())),
                        hasProperty("description", equalTo(testBooking.getItem().getDescription())),
                        hasProperty("available", equalTo(testBooking.getItem().getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getBooker().getName())),
                        hasProperty("email", equalTo(testBooking.getBooker().getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void findByItemOwnerIdAndEndIsBefore() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Booking> findEntities = bookingRepository.findByItemOwnerIdAndEndIsBefore(users.get(0).getId(),
                LocalDateTime.now(), pageRequest);
        Booking testBooking = bookings.get(0);

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(testBooking.getStart())),
                hasProperty("end", equalTo(testBooking.getEnd())),
                hasProperty("item", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getItem().getName())),
                        hasProperty("description", equalTo(testBooking.getItem().getDescription())),
                        hasProperty("available", equalTo(testBooking.getItem().getAvailable()))
                )),
                hasProperty("booker", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(testBooking.getBooker().getName())),
                        hasProperty("email", equalTo(testBooking.getBooker().getEmail()))
                )),
                hasProperty("status", equalTo(testBooking.getStatus()))
        ));
    }

    @Test
    void findByItem_IdAndItemOwnerId() {
        List<Booking> findEntities = bookingRepository.findByItem_IdAndItemOwnerId(items.get(0).getId(),
                users.get(0).getId());

        assertThat(findEntities, hasSize(2));
        for (Booking testBooking : List.of(bookings.get(0), bookings.get(1))) {
            assertThat(findEntities, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(testBooking.getStart())),
                    hasProperty("end", equalTo(testBooking.getEnd())),
                    hasProperty("item", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBooking.getItem().getName())),
                            hasProperty("description", equalTo(testBooking.getItem().getDescription())),
                            hasProperty("available", equalTo(testBooking.getItem().getAvailable()))
                    )),
                    hasProperty("booker", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBooking.getBooker().getName())),
                            hasProperty("email", equalTo(testBooking.getBooker().getEmail()))
                    )),
                    hasProperty("status", equalTo(testBooking.getStatus()))
            )));
        }
    }

    @Test
    void findTopByStatusNotLikeAndBookerIdAndItemId() {
        Optional<Booking> maybeFindEntity = bookingRepository
                .findTopByStatusNotLikeAndBookerIdAndItemId(BookingStatus.REJECTED, users.get(1).getId(),
                        items.get(0).getId(), Sort.by(Sort.Direction.ASC, "end"));
        Booking testBooking = bookings.get(0);

        if (maybeFindEntity.isPresent()) {
            Booking findEntity = maybeFindEntity.get();
            assertThat(findEntity, allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(testBooking.getStart())),
                    hasProperty("end", equalTo(testBooking.getEnd())),
                    hasProperty("item", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBooking.getItem().getName())),
                            hasProperty("description", equalTo(testBooking.getItem().getDescription())),
                            hasProperty("available", equalTo(testBooking.getItem().getAvailable()))
                    )),
                    hasProperty("booker", allOf(
                            hasProperty("id", notNullValue()),
                            hasProperty("name", equalTo(testBooking.getBooker().getName())),
                            hasProperty("email", equalTo(testBooking.getBooker().getEmail()))
                    )),
                    hasProperty("status", equalTo(testBooking.getStatus()))
            ));
        }
    }
}