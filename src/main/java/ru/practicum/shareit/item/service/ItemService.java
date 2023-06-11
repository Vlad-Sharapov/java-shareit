package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;

import java.util.List;

public interface ItemService {

    ItemDto saveItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long ownerId, Long itemId, ItemDto itemDto);

    List<ItemDtoByOwner> getAllUserItems(Long userId);

    ItemDtoByOwner getItem(Long userId, Long itemId);

    List<ItemDto> search(String text);

    CommentDto addComment(Long authorId, Long itemId, CommentDto commentDto);

}
