package ru.practicum.shareit.item.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.auxilary.GeneratorId;
import ru.practicum.shareit.exception.ObjectExistenceException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

    private final GeneratorId generatorId;
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item add(Item item) {
        item.setId(generatorId.incrementId());
        items.put(item.getId(), item);
        return items.get(item.getId());
    }

    @Override
    public Item update(Long ownerId, Item item) {
        Long id = item.getId();
        Item savedItem = items.get(id);
        checkItemOwner(savedItem, ownerId);
        if (item.getName() != null) {
            savedItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            savedItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            savedItem.setAvailable(item.getAvailable());
        }
        return savedItem;
    }

    @Override
    public List<Item> getAllFromUser(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Item get(Long userId, Long itemId) {
        checkItemExist(itemId);
        return items.get(itemId);
    }

    @Override
    public List<Item> getAll() {
        return new ArrayList<>(items.values());
    }

    private void checkItemExist(Long itemId) {
        if (items.get(itemId) == null) {
            throw new ObjectExistenceException("Item not found");
        }
    }

    private void checkItemOwner(Item item, Long ownerId) {
        if (!item.getOwner().equals(ownerId)) {
        throw new ObjectNotFoundException(String.format("The item was not found in the user %s", ownerId));
        }
    }
}
