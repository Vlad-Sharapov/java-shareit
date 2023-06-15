package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setOwnerId(item.getOwner().getId());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        if (item.getRequest() != null) {
            itemDto.setRequestId(item.getRequest().getId());
        }
        return itemDto;
    }

    public static List<ItemDto> toItemDto(Collection<Item> items) {
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public static ItemDtoByOwner toItemDtoByOwner(Item item, Booking lastBooking,
                                                  Booking nextBooking, List<Comment> comments) {
        ItemDtoByOwner itemDtoByOwner = new ItemDtoByOwner();
        itemDtoByOwner.setId(item.getId());
        itemDtoByOwner.setName(item.getName());
        itemDtoByOwner.setDescription(item.getDescription());
        itemDtoByOwner.setAvailable(item.getAvailable());
        if (item.getRequest() != null) {
            itemDtoByOwner.setRequestId(item.getRequest().getId());
        }
        if (lastBooking != null) {
            itemDtoByOwner.setLastBooking(BookingMapper.toBookingDto(lastBooking));
        }
        if (nextBooking != null) {
            itemDtoByOwner.setNextBooking(BookingMapper.toBookingDto(nextBooking));
        }
        itemDtoByOwner.setComments(CommentMapper.toCommentDto(comments));
        return itemDtoByOwner;
    }

    public static ItemDtoByOwner toItemDtoByOwner(Item item, List<Comment> userComments, List<Booking> userBookings) {
        LocalDateTime now = LocalDateTime.now();
        Booking nextBooking = userBookings.stream()
                .filter(booking -> booking.getStart().isAfter(now))
                .filter(booking -> booking.getStatus() != BookingStatus.REJECTED)
                .filter(booking -> booking.getItem().getId().equals(item.getId()))
                .min(Comparator.comparing(Booking::getStart)).orElse(null);
        Booking lastBooking = userBookings.stream()
                .filter(booking -> booking.getStart().isBefore(now))
                .filter(booking -> booking.getStatus() != (BookingStatus.REJECTED))
                .filter(booking -> booking.getItem().getId().equals(item.getId()))
                .max(Comparator.comparing(Booking::getStart)).orElse(null);
        return toItemDtoByOwner(item, lastBooking, nextBooking, userComments);
    }


    public static Item toItem(ItemDto itemDto, User user) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setOwner(user);
        item.setAvailable(itemDto.getAvailable());
        return item;
    }

    public static Item toItem(ItemDto itemDto, User user, ItemRequest itemRequest) {
        Item item = toItem(itemDto, user);
        item.setRequest(itemRequest);
        return item;
    }
}

