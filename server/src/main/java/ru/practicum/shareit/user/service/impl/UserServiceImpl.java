package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
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
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto add(UserDto userDto) {
        UserDto response = toUserDto(userRepository.save(toUser(userDto)));
        log.info("A new user has registered: {}", response);
        return response;
    }

    @Transactional
    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        log.info("User {} has updated his data.", userId);
        return toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto get(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        log.info("User {} is being viewed", userId);
        return toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        List<User> allUsers = userRepository.findAll();
        log.info("A list of all users has been received. Total users - {}", allUsers.size());
        return allUsers.stream()
                .map((UserMapper::toUserDto))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void delete(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        userRepository.delete(user);
        log.info("User {} has been deleted.", user);
    }

}
