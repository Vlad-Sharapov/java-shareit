package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    User user1 = User.builder().name("user1").email("newuser1@mail.ru").build();
    User user1EmptyEmail = User.builder().name("user1").build();
    User user1FailEmail = User.builder().name("user1").email("newuser1mailru").build();
    User user1EmptyName = User.builder().email("newuser1@mail.ru").build();
    User updateUser = User.builder().name("updateUser").email("updateUser@mail.ru").build();
    User userUpdateName = User.builder().name("updateUser2").build();
    User userUpdateEmail = User.builder().email("updateuser2@mail.ru").build();
    User userUpdateEmailExist = User.builder().email("newuser1@mail.ru").build();


    @Test
    void shouldUserCreateWhenUsePostUsers() throws Exception {

        this.mockMvc.perform(post("/users")
                        .content(asJsonString(user1)).contentType("application/json")
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void shouldUserCreateStatus500WhenUsePostUsersWithDuplicateEmail() throws Exception {
        this.mockMvc.perform(post("/users")
                .content(asJsonString(user1)).contentType("application/json")
                .accept("*/*"));
        this.mockMvc.perform(post("/users")
                        .content(asJsonString(user1)).contentType("application/json")
                        .accept("*/*"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldUserCreateWhenUsePostUsersWithEmptyEmail() throws Exception {
        this.mockMvc.perform(post("/users")
                        .content(asJsonString(user1EmptyEmail)).contentType("application/json")
                        .accept("*/*"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUserCreateWhenUsePostUsersWithInvalidEmail() throws Exception {
        this.mockMvc.perform(post("/users")
                        .content(asJsonString(user1FailEmail)).contentType("application/json")
                        .accept("*/*"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUserCreateWhenUsePostUsersWithEmptyName() throws Exception {
        this.mockMvc.perform(post("/users")
                        .content(asJsonString(user1EmptyName)).contentType("application/json")
                        .accept("*/*"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUserUpdateWhenUsePatchUsers() throws Exception {
        this.mockMvc.perform(post("/users")
                        .content(asJsonString(user1)).contentType("application/json")
                        .accept("*/*"))
                .andExpect(status().isOk());
        this.mockMvc.perform(patch("/users/{userId}", 1)
                        .content(asJsonString(updateUser)).contentType("application/json")
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updateUser"))
                .andExpect(jsonPath("$.email").value("updateUser@mail.ru"));


    }

    @Test
    void shouldUserUpdateNameWhenUsePatchUsers() throws Exception {
        this.mockMvc.perform(patch("/users/{userId}", 1)
                        .content(asJsonString(userUpdateName)).contentType("application/json")
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updateUser2"));
    }

    @Test
    void shouldUserUpdateEmailWhenUsePatchUsers() throws Exception {
        this.mockMvc.perform(patch("/users/{userId}", 1)
                        .content(asJsonString(userUpdateEmail)).contentType("application/json")
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updateuser2@mail.ru"));
    }

    @Test
    void shouldUserUpdateWithSameEmailWhenUsePatchUsers() throws Exception {
        this.mockMvc.perform(patch("/users/{userId}", 1)
                .content(asJsonString(userUpdateEmail)).contentType("application/json")
                .accept("*/*"));
        this.mockMvc.perform(patch("/users/{userId}", 1)
                        .content(asJsonString(userUpdateEmail)).contentType("application/json")
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updateuser2@mail.ru"));
    }

    @Test
    void shouldUserUpdateStatus500WhenUsePatchUsersWithEmailExist() throws Exception {
        this.mockMvc.perform(post("/users")
                .content(asJsonString(user1)).contentType("application/json")
                .accept("*/*"));
        this.mockMvc.perform(post("/users")
                .content(asJsonString(updateUser)).contentType("application/json")
                .accept("*/*"));
        this.mockMvc.perform(patch("/users/{userId}", 1)
                        .content(asJsonString(userUpdateEmailExist)).contentType("application/json")
                        .accept("*/*"))
                .andExpect(status().isInternalServerError());

    }

    @Test
    void shouldUserGetWhenUseGetUsersWithPathVariable() throws Exception {
        this.mockMvc.perform(get("/users/{userId}", 1)
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldUserGetAllWhenUseGetUsers() throws Exception {
        String contentAsString = this.mockMvc.perform(get("/users")
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        List<UserDto> userDtos = objectMapper.readValue(contentAsString,
                new TypeReference<>() {
                });

        assertEquals(userDtos.size(), 2);
    }

    @Test
    void deleteUser() throws Exception {
        this.mockMvc.perform(delete("/users/{userId}", 1)
                        .accept("*/*"))
                .andExpect(status().isOk());
        String contentAsString = this.mockMvc.perform(get("/users")
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        List<UserDto> userDtos = objectMapper.readValue(contentAsString,
                new TypeReference<>() {
                });

        assertEquals(userDtos.size(), 1);
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