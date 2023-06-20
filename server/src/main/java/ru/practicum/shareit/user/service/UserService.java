package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {


    UserDto add(UserDto userDto);

    UserDto update(Long userId, UserDto userDto);

    UserDto get(Long userId);

    List<UserDto> getAll();

    void delete(Long userId);

}
