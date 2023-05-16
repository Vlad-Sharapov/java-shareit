package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    Item add(Item item);

    Item update(Item item);

    List<Item> getAllFromUser(Long itemId);

    Item get(Long userId, Long itemId);

    List<Item> getAll();

}
