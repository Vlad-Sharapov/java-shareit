package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";


    UserDto userDto = UserDto.builder().name("user1").email("user1@mail.ru").build();

    ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("Дрель")
            .description("Обычная дрель")
            .ownerId(userDto.getId())
            .available(true)
            .build();

    ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(1L)
            .description("qwerty")
            .requestorId(userDto.getId())
            .items(List.of(itemDto))
            .created(LocalDateTime.now().withNano(0))
            .build();

    @Test
    void shouldItemRequestCreateWhenUsePostCreateRequest() throws Exception {
        when(itemRequestService.createRequest(Mockito.anyLong(), Mockito.any(ItemRequestDto.class)))
                .thenReturn(itemRequestDto);

        this.mockMvc.perform(post("/requests")
                        .content(asJsonString(itemRequestDto))
                        .header(USER_ID_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestDto.getDescription()))
                .andExpect(jsonPath("$.requestorId").value(itemRequestDto.getRequestorId()))
                .andExpect(jsonPath("$.created").value(itemRequestDto.getCreated().toString()))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0]").value(itemDto));
    }

    @Test
    void shouldThrowsStatus404WhenUsePostCreateRequestWithUserNotFound() throws Exception {
        when(itemRequestService.createRequest(Mockito.anyLong(), Mockito.any(ItemRequestDto.class)))
                .thenThrow(new EntityNotFoundException("not Found"));

        this.mockMvc.perform(post("/requests")
                        .content(asJsonString(itemRequestDto))
                        .header(USER_ID_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetUserRequestsCreateWhenUseUserRequests() throws Exception {

        when(itemRequestService.getUserRequests(Mockito.anyLong()))
                .thenReturn(List.of(itemRequestDto));

        this.mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$[0].description").value(itemRequestDto.getDescription()))
                .andExpect(jsonPath("$[0].requestorId").value(itemRequestDto.getRequestorId()))
                .andExpect(jsonPath("$[0].created").value(itemRequestDto.getCreated().toString()))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0]").value(itemDto));
    }

    @Test
    void shouldThrowsStatus404WhenUseGetUserRequestsWithUserNotFound() throws Exception {
        when(itemRequestService.getUserRequests(Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException("not Found"));

        this.mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .accept("*/*"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllRequestsCreateWhenUseAllRequests() throws Exception {
        when(itemRequestService.getAllRequests(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(itemRequestDto));

        this.mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 2L)
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$[0].description").value(itemRequestDto.getDescription()))
                .andExpect(jsonPath("$[0].requestorId").value(itemRequestDto.getRequestorId()))
                .andExpect(jsonPath("$[0].created").value(itemRequestDto.getCreated().toString()))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0]").value(itemDto));
    }

    @Test
    void shouldGetItemRequestCreateWhenUseItemRequest() throws Exception {
        when(itemRequestService.getRequest(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(itemRequestDto);

        this.mockMvc.perform(get("/requests/{id}", 1)
                        .header(USER_ID_HEADER, 2L)
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestDto.getDescription()))
                .andExpect(jsonPath("$.requestorId").value(itemRequestDto.getRequestorId()))
                .andExpect(jsonPath("$.created").value(itemRequestDto.getCreated().toString()))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0]").value(itemDto));

    }

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}