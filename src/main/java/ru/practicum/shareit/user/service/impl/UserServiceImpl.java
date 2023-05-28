package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.dto.UserMapper.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserDto add(UserDto userDto) {
        User user = toUser(userDto);
        return toUserDto(userRepository.add(user));
    }

    public UserDto update(Long userId, UserDto userDto) {
        User user = toUser(userDto);
        user.setId(userId);
        return toUserDto(userRepository.update(user));
    }

    public UserDto get(Long userId) {
        return toUserDto(userRepository.get(userId));
    }

    public List<UserDto> getAll() {
        List<User> allUsers = userRepository.getAll();
        return allUsers.stream()
                .map((UserMapper::toUserDto))
                .collect(Collectors.toList());
    }

    public void delete(Long userId) {
        userRepository.delete(userId);
    }

}
