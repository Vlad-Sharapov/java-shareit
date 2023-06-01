package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.utils.Marker;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto add(@RequestHeader(USER_ID_HEADER) Long userId,
                       @Validated({Marker.OnCreate.class}) @RequestBody ItemDto itemDto) {
        return itemService.saveItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_HEADER) long userId,
                          @Validated({Marker.OnUpdate.class}) @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);

    }


    @GetMapping
    public List<ItemDtoByOwner> getAllUser(@RequestHeader(USER_ID_HEADER) long userId) {
        return itemService.getAllItemsUser(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDtoByOwner get(@RequestHeader(USER_ID_HEADER) long userId,
                              @PathVariable Long itemId) {
        return itemService.getItem(userId, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_ID_HEADER) long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentDto commentDto) {
        return itemService.addComment(userId, itemId, commentDto);
    }
}

