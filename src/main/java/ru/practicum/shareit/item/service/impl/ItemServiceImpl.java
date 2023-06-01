package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.stream.Collectors;

import static ru.practicum.shareit.item.dto.ItemMapper.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemDto saveItem(Long userId, ItemDto itemDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found."));
        ItemDto response = toItemDto(itemRepository.save(toItem(itemDto, user)));
        log.info("User {} add item {}", userId, response);
        return response;
    }

    @Transactional
    @Override
    public ItemDto updateItem(Long ownerId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ObjectNotFoundException("Item not found."));
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ObjectNotFoundException(String.format("The item was not found in the user %s", ownerId));
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        log.info("Owner {} update item {}", ownerId, itemId);
        return toItemDto(itemRepository.save(item));
    }

    @Override
    public List<ItemDtoByOwner> getAllItemsUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found."));
        List<Item> userItems = itemRepository.findByOwnerId(userId);
        List<Booking> userBookings = bookingRepository.findByItemOwnerId(userId, Sort.by(Sort.Direction.DESC, "start"));
        List<Comment> userItemsComments = commentRepository.findByItem_IdIn(userItems.stream()
                .map(Item::getId)
                .collect(Collectors.toList()));
        log.info("User {} getting all of his items", userId);
        return userItems.stream()
                .map(item -> {
                    List<Comment> comments = userItemsComments.stream()
                            .filter(commentItem -> item.getId().equals(commentItem.getId()))
                            .collect(Collectors.toList());
                    List<Booking> bookings = userBookings.stream()
                            .filter(booking -> booking.getItem().getOwner().getId().equals(userId))
                            .filter(booking -> booking.getItem().getId().equals(item.getId()))
                            .filter(booking -> !booking.getStatus().equals(BookingStatus.REJECTED))
                            .collect(Collectors.toList());
                    Booking nextBooking = bookings.stream()
                            .filter(booking -> booking.getStart().isAfter(Instant.now()))
                            .min(Comparator.comparing(Booking::getStart)).orElse(null);
                    Booking lastBooking = bookings.stream()
                            .filter(booking -> booking.getStart().isBefore(Instant.now()))
                            .max(Comparator.comparing(Booking::getStart)).orElse(null);
                    return ItemMapper.toItemDtoByOwner(item, lastBooking, nextBooking, comments);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemDtoByOwner getItem(Long userId, Long itemId) {
        userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("User not found."));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ObjectNotFoundException("Item not found."));
        List<Booking> bookings = bookingRepository.findByItem_Id(itemId);
        List<Comment> comments = commentRepository.findByItem_Id(itemId);
        Booking nextBooking = bookings.stream()
                .filter(booking -> booking.getStart().isAfter(Instant.now()))
                .filter(booking -> booking.getItem().getOwner().getId().equals(userId))
                .filter(booking -> !booking.getStatus().equals(BookingStatus.REJECTED))
                .min(Comparator.comparing(Booking::getStart)).orElse(null);
        Booking lastBooking = bookings.stream()
                .filter(booking -> booking.getStart().isBefore(Instant.now()))
                .filter(booking -> booking.getItem().getOwner().getId().equals(userId))
                .filter(booking -> !booking.getStatus().equals(BookingStatus.REJECTED))
                .max(Comparator.comparing(Booking::getStart)).orElse(null);
        log.info("User {} getting an item with id - {}", userId, itemId);
        return toItemDtoByOwner(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        List<Item> items = itemRepository.findAllByDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(text, text);
        log.info("The user searches for items by text - \"{}\"", text);
        return items.stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto addComment(Long authorId, Long itemId, CommentDto commentDto) {
        User user = userRepository.findById(authorId).orElseThrow(() -> new ObjectNotFoundException("User not found."));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ObjectNotFoundException("Item not found."));
        Comment comment = CommentMapper.toComment(commentDto, user, item);
        Booking booking = bookingRepository
                .findTopByStatusNotLikeAndBookerIdAndItemId(BookingStatus.REJECTED, authorId, itemId,
                        Sort.by(Sort.Direction.ASC, "end"))
                .orElseThrow(() -> new BadRequestException(String
                        .format("The user %s didn't rent the item %s", user.getName(), item.getName())));
        if (comment.getCreated().isAfter(booking.getEnd())) {
            log.info("User {} wrote a comment", authorId);
            return CommentMapper.toCommentDto(commentRepository.save(comment));
        }
        throw new BadRequestException("User hasn't completed the rental of the item yet.");
    }

}
