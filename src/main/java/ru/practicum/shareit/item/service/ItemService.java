package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;

import java.util.List;

public interface ItemService {

    ItemDto saveItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long ownerId, Long itemId, ItemDto itemDto);

    List<ItemDtoByOwner> getUserItems(Long userId, int from, int size);

    ItemDtoByOwner getItem(Long userId, Long itemId);

    List<ItemDto> search(String text, int from, int size);

    CommentDto addComment(Long authorId, Long itemId, CommentDto commentDto);

}
