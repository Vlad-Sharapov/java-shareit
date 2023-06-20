package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.utils.Marker;

/**
 * TODO Sprint add-controllers.
 */

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {

    private final UserClient userClient;

    @PostMapping(value = {"", "/{userId}"})
    public ResponseEntity<Object> create(@Validated({Marker.OnCreate.class}) @RequestBody UserDto userDto,
                                         @PathVariable(required = false) String userId) {
        return userClient.create(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable Long userId,
                                         @Validated({Marker.OnUpdate.class}) @RequestBody UserDto userDto) {
        return userClient.update(userId, userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> user(@PathVariable Long userId) {
        return userClient.getUser(userId);
    }

    @GetMapping
    public ResponseEntity<Object> allUsers() {
        return userClient.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@PathVariable Long userId) {
        return userClient.deleteUser(userId);
    }
}
