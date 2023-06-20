package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.utils.Marker;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

/**
 * TODO Sprint add-controllers.
 */
@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemClient itemClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(USER_ID_HEADER) Long userId,
                                      @Validated({Marker.OnCreate.class}) @RequestBody ItemDto itemDto) {
        return itemClient.saveItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(USER_ID_HEADER) long userId,
                                         @Validated({Marker.OnUpdate.class}) @PathVariable Long itemId,
                                         @RequestBody ItemDto itemDto) {
        return itemClient.updateItem(userId, itemId, itemDto);

    }

    @GetMapping
    public ResponseEntity<Object> allUserItems(@RequestHeader(USER_ID_HEADER) long userId,
                                               @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                               @Positive @RequestParam(defaultValue = "10") int size) {
        return itemClient.allUserItems(userId, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> item(@RequestHeader(USER_ID_HEADER) long userId,
                                       @PathVariable Long itemId) {
        return itemClient.item(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(USER_ID_HEADER) long userId,
                                         @RequestParam String text,
                                         @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                         @Positive @RequestParam(defaultValue = "10") int size) {
        return itemClient.search(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID_HEADER) long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody CommentDto commentDto) {
        return itemClient.addComment(userId, itemId, commentDto);
    }
}

