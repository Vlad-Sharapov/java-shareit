package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.Instant;
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
        checkDateTimeInterval(bookingDto.getStart(), bookingDto.getEnd());
        User user = userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found"));
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(() -> new ObjectNotFoundException("Item not found.")); // nullPoiEx?
        if (userId.equals(item.getOwner().getId())) {
            throw new ObjectNotFoundException("Booker is an owner");
        }
        if (item.getAvailable()) {
            Booking saveBooking = bookingRepository.save(BookingMapper.toBooking(bookingDto, user, item));
            saveBooking.setStatus(BookingStatus.WAITING);
            bookingRepository.save(saveBooking);
            log.info("Booker {} add booking {}.", userId, saveBooking);
            return BookingMapper.toBookingDtoOutput(saveBooking);
        }
        throw new BadRequestException("The item is already booked");
    }

    @Transactional
    @Override
    public BookingDtoOutput approved(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ObjectNotFoundException("Booking not found"));
        boolean checkOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!checkOwner) {
            throw new ObjectNotFoundException("Permission denied");
        }
        if (approved && booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new BadRequestException("Booking already is APPROVED");
        }
        if (!approved && booking.getStatus().equals(BookingStatus.REJECTED)) {
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
    public BookingDtoOutput get(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ObjectNotFoundException("Booking not found"));
        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
        boolean checkOwnerOrBooker = ownerId.equals(userId) || bookerId.equals(userId);
        if (!checkOwnerOrBooker) {
            throw new ObjectNotFoundException("Permission denied");
        }
        log.info("User {} getting an booking with id - {}", userId, bookingId);
        return BookingMapper.toBookingDtoOutput(booking);
    }

    @Transactional
    @Override
    public List<BookingDtoOutput> getAllFromUser(Long userId, String status) {
        checkEnumExist(status);
        userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found"));
        List<Booking> bookings = new ArrayList<>();
        BookingStatus bookingStatus = BookingStatus.valueOf(status);
        if (bookingStatus.equals(BookingStatus.ALL)) {
            bookings = bookingRepository.findByBookerId(userId, Sort.by(Sort.Direction.DESC, "start"));
        }
        if (bookingStatus.equals(BookingStatus.WAITING)) {
            bookings = bookingRepository.findByBookerIdAndStatus(userId, bookingStatus, Sort.by(Sort.Direction.DESC, "start"));
        }
        if (bookingStatus.equals(BookingStatus.REJECTED)) {
            bookings = bookingRepository.findByBookerIdAndStatus(userId, bookingStatus, Sort.by(Sort.Direction.DESC, "start"));
        }
        if (bookingStatus.equals(BookingStatus.CURRENT)) {
            bookings = bookingRepository.findByBookerIdAndEndIsAfterAndStartIsBefore(userId, Instant.now(), Instant.now(), Sort.by(Sort.Direction.DESC, "start"));
        }
        if (bookingStatus.equals(BookingStatus.PAST)) {
            bookings = bookingRepository.findByBookerIdAndEndIsBefore(userId, Instant.now(), Sort.by(Sort.Direction.DESC, "start"));
        }
        if (bookingStatus.equals(BookingStatus.FUTURE)) {
            bookings = bookingRepository.findByBookerIdAndStartIsAfter(userId, Instant.now(), Sort.by(Sort.Direction.DESC, "start"));
        }
        log.info("Booker {} getting all of his bookings with state: {}", userId, status);
        return bookings.stream()
                .map(BookingMapper::toBookingDtoOutput)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDtoOutput> getAllFromOwner(Long userId, String status) {
        checkEnumExist(status);
        userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found"));
        List<Booking> bookings = new ArrayList<>();
        BookingStatus bookingStatus = BookingStatus.valueOf(status);
        if (bookingStatus.equals(BookingStatus.ALL)) {
            bookings = bookingRepository.findByItemOwnerId(userId, Sort.by(Sort.Direction.DESC, "start"));
        }
        if (bookingStatus.equals(BookingStatus.WAITING)) {
            bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, bookingStatus, Sort.by(Sort.Direction.DESC, "start"));
        }
        if (bookingStatus.equals(BookingStatus.REJECTED)) {
            bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, bookingStatus, Sort.by(Sort.Direction.DESC, "start"));
        }
        if (bookingStatus.equals(BookingStatus.CURRENT)) {
            bookings = bookingRepository.findByItemOwnerIdAndEndIsAfterAndStartIsBefore(userId, Instant.now(), Instant.now(), Sort.by(Sort.Direction.DESC, "start"));
        }
        if (bookingStatus.equals(BookingStatus.PAST)) {
            bookings = bookingRepository.findByItemOwnerIdAndEndIsBefore(userId, Instant.now(), Sort.by(Sort.Direction.DESC, "start"));
        }
        if (bookingStatus.equals(BookingStatus.FUTURE)) {
            bookings = bookingRepository.findByItemOwnerIdAndStartIsAfter(userId, Instant.now(), Sort.by(Sort.Direction.DESC, "start"));
        }
        log.info("Owner {} getting all of his bookings with state: {}", userId, status);
        return bookings.stream()
                .map(BookingMapper::toBookingDtoOutput)
                .collect(Collectors.toList());
    }

    private void checkDateTimeInterval(LocalDateTime startDate, LocalDateTime endDate) {
        Boolean a = !endDate.isAfter(startDate);
        Boolean b = endDate.equals(startDate);
        if (a || b) {
            throw new BadRequestException("Invalid date interval");
        }
    }

    private void checkEnumExist(String state) {
        for (BookingStatus bookingStatus : BookingStatus.values()) {
            if (bookingStatus.name().equals(state)) {
                return;
            }
        }
        throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
    }
}
