package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {

    User add(User user);

    User update(User user);

    User get(Long userId);

    List<User> getAll();

    void delete(Long userId);

}
