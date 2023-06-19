package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    @Override
    public BookingDtoOutput add(Long userId, BookingDto bookingDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(() -> new EntityNotFoundException("Item not found."));
        if (userId.equals(item.getOwner().getId())) {
            throw new EntityNotFoundException("Booker is an owner");
        }
        if (!item.getAvailable()) {
            throw new BadRequestException("The item is already booked");
        }
        Booking saveBooking = bookingRepository.save(BookingMapper.toBooking(bookingDto, user, item));
        log.info("Booker {} add booking {}.", userId, saveBooking);
        return BookingMapper.toBookingDtoOutput(saveBooking);
    }

    @Transactional
    @Override
    public BookingDtoOutput approved(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        boolean checkOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!checkOwner) {
            throw new EntityNotFoundException("Permission denied");
        }
        if (approved && booking.getStatus() == BookingStatus.APPROVED) {
            throw new BadRequestException("Booking already is APPROVED");
        }
        if (!approved && booking.getStatus() == BookingStatus.REJECTED) {
            throw new BadRequestException("Booking already is REJECTED");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
            log.info("Owner {} approve booking {}. ", userId, bookingId);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
            log.info("Owner {} not approve booking {}. ", userId, bookingId);
        }
        bookingRepository.save(booking);
        return BookingMapper.toBookingDtoOutput(booking);
    }

    @Override
    public BookingDtoOutput getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
        boolean checkOwnerOrBooker = ownerId.equals(userId) || bookerId.equals(userId);
        if (!checkOwnerOrBooker) {
            throw new EntityNotFoundException("Permission denied");
        }
        log.info("User {} getting an booking with id - {}", userId, bookingId);
        return BookingMapper.toBookingDtoOutput(booking);
    }

    @Transactional
    @Override
    public List<BookingDtoOutput> getAllUserBookings(Long userId, String status, int from, int size) {
        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.DESC, "start"));
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<Booking> bookings = new ArrayList<>();
        BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
        if (bookingStatus == BookingStatus.ALL) {
            bookings = bookingRepository.findByBookerId(userId, pageRequest);
        }
        if (bookingStatus == BookingStatus.WAITING) {
            bookings = bookingRepository.findByBookerIdAndStatus(userId, bookingStatus, pageRequest);
        }
        if (bookingStatus == BookingStatus.REJECTED) {
            bookings = bookingRepository.findByBookerIdAndStatus(userId, bookingStatus,pageRequest);
        }
        if (bookingStatus == BookingStatus.CURRENT) {
            bookings = bookingRepository.findByBookerIdAndEndIsAfterAndStartIsBefore(userId, now, now, pageRequest);
        }
        if (bookingStatus == BookingStatus.PAST) {
            bookings = bookingRepository.findByBookerIdAndEndIsBefore(userId, now, pageRequest);
        }
        if (bookingStatus == BookingStatus.FUTURE) {
            bookings = bookingRepository.findByBookerIdAndStartIsAfter(userId, now, pageRequest);
        }
        log.info("Booker {} getting all of his bookings with state: {}", userId, status);
        return bookings.stream()
                .map(BookingMapper::toBookingDtoOutput)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDtoOutput> getAllOwnerBookings(Long userId, String status, int from, int size) {
        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.DESC, "start"));
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<Booking> bookings = new ArrayList<>();
        BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
        if (bookingStatus == BookingStatus.ALL) {
            bookings = bookingRepository.findByItemOwnerId(userId, pageRequest);
        }
        if (bookingStatus == BookingStatus.WAITING) {
            bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, bookingStatus, pageRequest);
        }
        if (bookingStatus == BookingStatus.REJECTED) {
            bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, bookingStatus, pageRequest);
        }
        if (bookingStatus == BookingStatus.CURRENT) {
            bookings = bookingRepository.findByItemOwnerIdAndEndIsAfterAndStartIsBefore(userId, now, now, pageRequest);
        }
        if (bookingStatus == BookingStatus.PAST) {
            bookings = bookingRepository.findByItemOwnerIdAndEndIsBefore(userId, now, pageRequest);
        }
        if (bookingStatus == BookingStatus.FUTURE) {
            bookings = bookingRepository.findByItemOwnerIdAndStartIsAfter(userId, now, pageRequest);
        }
        log.info("Owner {} getting all of his bookings with state: {}", userId, status);
        return bookings.stream()
                .map(BookingMapper::toBookingDtoOutput)
                .collect(Collectors.toList());
    }

}
