package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    UserDto userDto1 = UserDto.builder().id(1L).name("user1").email("newuser1@mail.ru").build();
    UserDto user1EmptyEmail = UserDto.builder().name("user1").build();
    UserDto updateUser = UserDto.builder().id(1L).name("updateUser").email("updateUser@mail.ru").build();
    UserDto userUpdateEmailExist = UserDto.builder().email("newuser1@mail.ru").build();


    @Test
    void shouldUserCreateWhenUsePostUsers() throws Exception {
        when(userService.add(Mockito.any(UserDto.class)))
                .thenReturn(userDto1);

        this.mockMvc.perform(post("/users")
                        .content(asJsonString(userDto1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto1.getId()))
                .andExpect(jsonPath("$.name").value(userDto1.getName()))
                .andExpect(jsonPath("$.email").value(userDto1.getEmail()));

    }


    @Test
    void shouldUserCreateStatus500WhenUsePostUsersWithDuplicateEmail() throws Exception {
        when(userService.add(Mockito.any(UserDto.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate email"));
        this.mockMvc.perform(post("/users")
                        .content(asJsonString(userDto1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldUserCreateWhenUsePostUsersWithBadRequest() throws Exception {
        when(userService.add(Mockito.any(UserDto.class)))
                .thenThrow(new BadRequestException("Bad request"));
        this.mockMvc.perform(post("/users")
                        .content(asJsonString(user1EmptyEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("*/*"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUserUpdateWhenUsePatchUsers() throws Exception {
        when(userService.update(Mockito.anyLong(), Mockito.any(UserDto.class)))
                .thenReturn(updateUser);
        this.mockMvc.perform(patch("/users/{userId}", 1)
                        .content(asJsonString(updateUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updateUser.getId()))
                .andExpect(jsonPath("$.name").value(updateUser.getName()))
                .andExpect(jsonPath("$.email").value(updateUser.getEmail()));
    }

    @Test
    void shouldUserUpdateStatus500WhenUsePatchUsersWithEmailExist() throws Exception {
        when(userService.update(Mockito.anyLong(), Mockito.any(UserDto.class)))
                .thenThrow(new DataIntegrityViolationException("conflict"));
        this.mockMvc.perform(patch("/users/{userId}", 1)
                        .content(asJsonString(userUpdateEmailExist))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("*/*"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldUserGetWhenUseGetUsersWithPathVariable() throws Exception {
        when(userService.get(Mockito.anyLong()))
                .thenReturn(userDto1);
        this.mockMvc.perform(get("/users/{userId}", 1)
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto1.getId()))
                .andExpect(jsonPath("$.name").value(userDto1.getName()))
                .andExpect(jsonPath("$.email").value(userDto1.getEmail()));
    }

    @Test
    void shouldUserGetAllWhenUseGetUsers() throws Exception {
        when(userService.getAll())
                .thenReturn(List.of(userDto1));
        this.mockMvc.perform(get("/users")
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userDto1.getId()))
                .andExpect(jsonPath("$[0].name").value(userDto1.getName()))
                .andExpect(jsonPath("$[0].email").value(userDto1.getEmail()));
    }

    @Test
    void deleteUser() throws Exception {

        this.mockMvc.perform(delete("/users/{userId}", 2)
                        .accept("*/*"))
                .andExpect(status().isOk());
        verify(userService, Mockito.times(1)).delete(2L);
    }

    public static String asJsonString(final Object obj) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}