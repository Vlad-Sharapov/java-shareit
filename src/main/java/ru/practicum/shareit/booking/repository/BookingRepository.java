package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByBookerIdAndEndIsAfterAndStartIsBefore(Long bookerId, Instant end, Instant start, Sort sort);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, Instant start, Sort sort);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, Instant end, Sort sort);


    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    List<Booking> findByItemOwnerIdAndEndIsAfterAndStartIsBefore(Long ownerId, Instant end, Instant start, Sort sort);

    List<Booking> findByItemOwnerIdAndStartIsAfter(Long ownerId, Instant start, Sort sort);

    List<Booking> findByItemOwnerIdAndEndIsBefore(Long ownerId, Instant end, Sort sort);

    List<Booking> findByItem_Id(Long itemId);


    Optional<Booking> findTopByStatusNotLikeAndBookerIdAndItemId(BookingStatus status, Long bookerId, Long itemId, Sort sort);


}
