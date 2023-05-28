package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto addNewItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    List<ItemDto> getAllItemsUser(Long userId);

    ItemDto getItem(Long userId, Long itemId);

    List<ItemDto> search(String text);

}
