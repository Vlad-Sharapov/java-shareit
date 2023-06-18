package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByBookerIdAndEndIsAfterAndStartIsBefore(Long bookerId, LocalDateTime end, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerId(Long ownerId, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndIsAfterAndStartIsBefore(Long ownerId, LocalDateTime end, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartIsAfter(Long ownerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndIsBefore(Long ownerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByItem_IdAndItemOwnerId(Long itemId, Long itemOwnerId);

    Optional<Booking> findTopByStatusNotLikeAndBookerIdAndItemId(BookingStatus status, Long bookerId, Long itemId, Sort sort);

}
