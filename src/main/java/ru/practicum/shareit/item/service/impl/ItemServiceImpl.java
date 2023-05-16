package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.dto.ItemMapper.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto addNewItem(Long userId, ItemDto itemDto) {
        userRepository.get(userId);
        Item item = toItem(itemDto);
        item.setOwner(userId);
        return toItemDto(itemRepository.add(item));
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        userRepository.get(userId);
        Item item = toItem(itemDto);
        item.setId(itemId);
        item.setOwner(userId);
        return toItemDto(itemRepository.update(item));
    }

    @Override
    public List<ItemDto> getAllItemsUser(Long userId) {
        userRepository.get(userId);
        List<Item> allItemsUser = itemRepository.getAllFromUser(userId);
        return allItemsUser.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItem(Long userId, Long itemId) {
        userRepository.get(userId);
        return toItemDto(itemRepository.get(userId, itemId));
    }

    @Override
    public List<ItemDto> search(String text) {
        if (!text.isBlank()) {
            String lowerText = text.toLowerCase();
            List<Item> items = itemRepository.getAll();
            return items.stream()
                    .filter(item -> item.getName().toLowerCase().contains(lowerText) ||
                            item.getDescription().toLowerCase().contains(lowerText))
                    .filter(item -> item.getAvailable().equals(true))
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


}
