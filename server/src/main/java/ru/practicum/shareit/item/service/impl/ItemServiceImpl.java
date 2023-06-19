package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
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
    private final ItemRequestRepository itemRequestRepository;

    @Transactional
    @Override
    public ItemDto saveItem(Long userId, ItemDto itemDto) {
        ItemRequest itemRequest = null;
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found."));
        if (itemDto.getRequestId() != null) {
            itemRequest = itemRequestRepository.findById(itemDto.getRequestId()).orElse(null);
        }
        ItemDto response = toItemDto(itemRepository.save(toItem(itemDto, user, itemRequest)));
        log.info("User {} add item {}", userId, response);
        return response;
    }

    @Transactional
    @Override
    public ItemDto updateItem(Long ownerId, Long itemId, ItemDto itemDto) {
        Item item = getItemAndCheckPermission(itemId, ownerId);
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
    public List<ItemDtoByOwner> getUserItems(Long userId, int from, int size) {
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found."));
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.DESC, "start"));
        List<Item> userItems = itemRepository.findByOwnerId(userId);
        List<Booking> bookings = bookingRepository.findByItemOwnerId(userId, pageRequest);
        List<Comment> comments = commentRepository.findByItem_IdIn(userItems.stream()
                .map(Item::getId)
                .collect(Collectors.toList()));
        log.info("User {} getting all of his items", userId);
        return userItems.stream()
                .map(item -> toItemDtoByOwner(item, comments, bookings))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDtoByOwner getItem(Long userId, Long itemId) {
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found."));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("Item not found."));
        List<Booking> bookings = bookingRepository.findByItem_IdAndItemOwnerId(itemId, userId);
        List<Comment> comments = commentRepository.findByItem_Id(itemId);
        log.info("User {} getting an item with id - {}", userId, itemId);
        return toItemDtoByOwner(item, comments, bookings);
    }

    @Override
    public List<ItemDto> search(String text, int from, int size) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        List<Item> items = itemRepository.findAllByDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(text, text, pageRequest);
        log.info("The user searches for items by text - \"{}\"", text);
        return items.stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto addComment(Long authorId, Long itemId, CommentDto commentDto) {
        User user = userRepository.findById(authorId).orElseThrow(() -> new EntityNotFoundException("User not found."));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("Item not found."));
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

    private Item getItemAndCheckPermission(long itemId, long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("The item with id " + itemId + " was not found"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new EntityNotFoundException(String.format("The item was not found in the user %s", userId));
        }
        return item;
    }
}
