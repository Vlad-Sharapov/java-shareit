package ru.practicum.shareit.user.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureTestDatabase
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

@Mock
    private UserRepository userRepository;

    private UserService userService;
    User user1 = User.builder().id(1L).name("user1").email("newuser1@mail.ru").build();
    User user1Updated = User.builder().id(1L).name("user1Update").email("newuser1update@mail.ru").build();
    UserDto user1Dto = UserDto.builder().name("user1").email("newuser1@mail.ru").build();

    @BeforeEach
    public void beforeEach() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void add() {
        when(userRepository.save(any(User.class)))
                .thenReturn(user1);
        UserDto newUser = userService.add(user1Dto);
        assertThat(newUser, allOf(
                hasProperty("id", equalTo(user1.getId())),
                hasProperty("name", equalTo(user1Dto.getName())),
                hasProperty("email", equalTo(user1Dto.getEmail()))
        ));
    }

    @Test
    void shouldAllFieldsUpdateWhenUseUpdate() {
        when(userRepository.save(any(User.class)))
                .thenReturn(user1Updated);
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(user1));
        UserDto newUser = userService.update(user1.getId(), UserDto.builder()
                .name(user1Updated.getName())
                .email(user1Updated.getEmail())
                .build());

        assertThat(newUser, allOf(
                hasProperty("id", equalTo(user1.getId())),
                hasProperty("name", equalTo(user1Updated.getName())),
                hasProperty("email", equalTo(user1Updated.getEmail())
                )));
        verify(userRepository, Mockito.times(1)).save(User.builder()
                .id(user1.getId())
                .name("user1Update")
                .email("newuser1update@mail.ru")
                .build());
    }

    @Test
    void shouldEmailUpdateWhenUseUpdate() {
        User updatedUser = user1.toBuilder().email(user1Updated.getEmail()).build();

        when(userRepository.save(any(User.class)))
                .thenReturn(updatedUser);
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(user1));
        UserDto newUser = userService.update(user1.getId(), UserDto.builder()
                .email(user1Updated.getEmail())
                .build());

        assertThat(newUser, allOf(
                hasProperty("id", equalTo(updatedUser.getId())),
                hasProperty("name", equalTo(updatedUser.getName())),
                hasProperty("email", equalTo(updatedUser.getEmail())
                )));

        verify(userRepository, Mockito.times(1)).save(User.builder()
                .id(user1.getId())
                .name(user1.getName())
                .email("newuser1update@mail.ru")
                .build());
    }

    @Test
    void shouldNameUpdateWhenUseUpdate() {
        User updatedUser = user1.toBuilder().name(user1Updated.getName()).build();


        when(userRepository.save(any(User.class)))
                .thenReturn(updatedUser);
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(user1));
        UserDto newUser = userService.update(user1.getId(), UserDto.builder()
                .name(updatedUser.getName())
                .build());

        assertThat(newUser, allOf(
                hasProperty("id", equalTo(updatedUser.getId())),
                hasProperty("name", equalTo(updatedUser.getName())),
                hasProperty("email", equalTo(updatedUser.getEmail())
                )));

        verify(userRepository, Mockito.times(1)).save(updatedUser.toBuilder()
                .build());
    }

    @Test
    void shouldThrowWhenUseUpdateWithUnknownUser() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> userService.update(user1.getId(), user1Dto.toBuilder().build()));
    }

}