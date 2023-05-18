package ru.practicum.shareit.user.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.utils.GeneratorId;
import ru.practicum.shareit.exception.ObjectExistenceException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final GeneratorId generatorId;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User add(User user) {
        checkEmailExist(user);
        user.setId(generatorId.incrementId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        User savedUser = users.get(user.getId());
        String newName = user.getName();
        String newEmail = user.getEmail();
        checkUserExist(user.getId());
        if (user.getName() != null) {
            savedUser.setName(newName);
        }
        if (user.getEmail() != null) {
            checkEmailExist(user);
            savedUser.setEmail(newEmail);
        }
        return savedUser;
    }

    @Override
    public User get(Long userId) {
        checkUserExist(userId);
        return users.get(userId);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(Long userId) {
        checkUserExist(userId);
        users.remove(userId);
    }

    private void checkUserExist(Long userId) {
        if (users.get(userId) == null) {
            throw new ObjectNotFoundException("User not found");
        }
    }

    private void checkEmailExist(User user) {
        boolean findEmail = users.values().stream()
                .anyMatch(saveUser -> saveUser.getEmail().equals(user.getEmail())
                        && !saveUser.getId().equals(user.getId()));
        if (findEmail) {
            throw new ObjectExistenceException("Email already exist");
        }
    }
}
