package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.enums.BookingStatus.APPROVED;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @MockBean
    private ItemService itemService;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";


    UserDto user1 = UserDto.builder().name("user1").email("user1@mail.ru").build();

    ItemDto item1 = ItemDto.builder()
            .name("Дрель")
            .description("Обычная дрель")
            .available(true)
            .build();
    ItemDto item1Output = ItemDto.builder()
            .name("Дрель")
            .description("Обычная дрель")
            .ownerId(user1.getId())
            .available(true)
            .build();
    ItemDto updateItem1 = ItemDto.builder()
            .name("Дрель+")
            .description("Аккумуляторная дрель")
            .ownerId(user1.getId())
            .available(false)
            .build();

    CommentDto commentDto = CommentDto.builder()
            .id(1L)
            .created(LocalDateTime.now().withNano(0))
            .text("qwerty")
            .authorName(user1.getName())
            .build();

    BookingDto bookingDto1 = BookingDto.builder()
            .id(1L)
            .itemId(item1.getId())
            .bookerId(user1.getId())
            .start(LocalDateTime.now().plusDays(1).withNano(0))
            .end(LocalDateTime.now().plusDays(2).withNano(0))
            .status(APPROVED)
            .build();

    BookingDto bookingDto2 = BookingDto.builder()
            .id(2L)
            .itemId(item1.getId())
            .bookerId(user1.getId())
            .start(LocalDateTime.now().minusDays(3).withNano(0))
            .end(LocalDateTime.now().minusDays(2).withNano(0))
            .status(APPROVED)
            .build();

    ItemDtoByOwner itemDtoByOwner = ItemDtoByOwner.builder()
            .name("Дрель")
            .description("Обычная дрель")
            .comments(List.of(commentDto))
            .lastBooking(bookingDto2)
            .nextBooking(bookingDto1)
            .available(true)
            .build();


    @Test
    void shouldItemCreateWhenUsePostItems() throws Exception {
        when(itemService.saveItem(Mockito.anyLong(), Mockito.any(ItemDto.class)))
                .thenReturn(item1Output);

        this.mockMvc.perform(post("/items")
                        .content(asJsonString(item1))
                        .header(USER_ID_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item1Output.getId()))
                .andExpect(jsonPath("$.name").value(item1Output.getName()))
                .andExpect(jsonPath("$.description").value(item1Output.getDescription()))
                .andExpect(jsonPath("$.ownerId").value(item1Output.getOwnerId()));
    }

    @Test
    void shouldStatus400WhenUsePostItemsWithoutXSharerUserId() throws Exception {
        this.mockMvc.perform(post("/items")
                        .content(asJsonString(item1))
                        .accept("*/*"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldStatus400WhenUsePostItemsWithUserOrItemNotFound() throws Exception {
        when(itemService.saveItem(Mockito.anyLong(), Mockito.any(ItemDto.class)))
                .thenThrow(new EntityNotFoundException("not found"));

        this.mockMvc.perform(post("/items")
                        .content(asJsonString(item1)).contentType("application/json")
                        .header(USER_ID_HEADER, 111L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    void shouldItemUpdateWhenUsePatchItems() throws Exception {
        when(itemService.updateItem(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(ItemDto.class)))
                .thenReturn(updateItem1);

        this.mockMvc.perform(patch("/items/{itemId}", 1)
                        .content(asJsonString(updateItem1)).contentType("application/json")
                        .header(USER_ID_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updateItem1.getId()))
                .andExpect(jsonPath("$.name").value(updateItem1.getName()))
                .andExpect(jsonPath("$.description").value(updateItem1.getDescription()))
                .andExpect(jsonPath("$.ownerId").value(updateItem1.getOwnerId()));
    }

    @Test
    void shouldItemUpdateStatus400WhenUsePatchItemsWithoutHeader() throws Exception {
        this.mockMvc.perform(patch("/items/{itemId}", 1)
                        .content(asJsonString(updateItem1)).contentType("application/json")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldItemUpdateStatus400WhenUsePatchItemsWhereItemOrUserNotFound() throws Exception {
        when(itemService.updateItem(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(ItemDto.class)))
                .thenThrow(new EntityNotFoundException("not found"));

        this.mockMvc.perform(patch("/items/{itemId}", 1)
                        .content(asJsonString(updateItem1)).contentType("application/json")
                        .header(USER_ID_HEADER, 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetItemWhenUseGetItemsWithParameterItemId() throws Exception {
        when(itemService.getItem(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(itemDtoByOwner);

        this.mockMvc.perform(get("/items/{itemId}", 1)
                        .content(asJsonString(item1)).contentType("application/json")
                        .header(USER_ID_HEADER, 1L)
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDtoByOwner.getId()))
                .andExpect(jsonPath("$.name").value(itemDtoByOwner.getName()))
                .andExpect(jsonPath("$.description").value(itemDtoByOwner.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDtoByOwner.getAvailable()))
                .andExpect(jsonPath("$.lastBooking.id").value(itemDtoByOwner.getLastBooking().getId()))
                .andExpect(jsonPath("$.lastBooking.start").value(bookingDto2.getStart().toString()))
                .andExpect(jsonPath("$.lastBooking.end").value(bookingDto2.getEnd().toString()))
                .andExpect(jsonPath("$.lastBooking.itemId").value(bookingDto2.getItemId()))
                .andExpect(jsonPath("$.lastBooking.bookerId").value(bookingDto2.getBookerId()))
                .andExpect(jsonPath("$.lastBooking.status").value(bookingDto2.getStatus().toString()))
                .andExpect(jsonPath("$.nextBooking.id").value(bookingDto1.getId()))
                .andExpect(jsonPath("$.nextBooking.start").value(bookingDto1.getStart().toString()))
                .andExpect(jsonPath("$.nextBooking.end").value(bookingDto1.getEnd().toString()))
                .andExpect(jsonPath("$.nextBooking.itemId").value(bookingDto1.getItemId()))
                .andExpect(jsonPath("$.nextBooking.bookerId").value(bookingDto1.getBookerId()))
                .andExpect(jsonPath("$.nextBooking.status").value(bookingDto1.getStatus().toString()))
                .andExpect(jsonPath("$.comments[0].id").value(commentDto.getId()));
    }

    @Test
    void shouldGetAllItemsForUserWhenUseGetItems() throws Exception {
        when(itemService.getUserItems(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(itemDtoByOwner));

        this.mockMvc.perform(get("/items", 1)
                        .content(asJsonString(item1)).contentType("application/json")
                        .header(USER_ID_HEADER, 1L)
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(itemDtoByOwner.getId()))
                .andExpect(jsonPath("$[0].name").value(itemDtoByOwner.getName()))
                .andExpect(jsonPath("$[0].description").value(itemDtoByOwner.getDescription()))
                .andExpect(jsonPath("$[0].available").value(itemDtoByOwner.getAvailable()))
                .andExpect(jsonPath("$[0].lastBooking.id").value(itemDtoByOwner.getLastBooking().getId()))
                .andExpect(jsonPath("$[0].lastBooking.start").value(bookingDto2.getStart().toString()))
                .andExpect(jsonPath("$[0].lastBooking.end").value(bookingDto2.getEnd().toString()))
                .andExpect(jsonPath("$[0].lastBooking.itemId").value(bookingDto2.getItemId()))
                .andExpect(jsonPath("$[0].lastBooking.bookerId").value(bookingDto2.getBookerId()))
                .andExpect(jsonPath("$[0].lastBooking.status").value(bookingDto2.getStatus().toString()))
                .andExpect(jsonPath("$[0].nextBooking.id").value(bookingDto1.getId()))
                .andExpect(jsonPath("$[0].nextBooking.start").value(bookingDto1.getStart().toString()))
                .andExpect(jsonPath("$[0].nextBooking.end").value(bookingDto1.getEnd().toString()))
                .andExpect(jsonPath("$[0].nextBooking.itemId").value(bookingDto1.getItemId()))
                .andExpect(jsonPath("$[0].nextBooking.bookerId").value(bookingDto1.getBookerId()))
                .andExpect(jsonPath("$[0].nextBooking.status").value(bookingDto1.getStatus().toString()))
                .andExpect(jsonPath("$[0].comments[0].id").value(commentDto.getId()));

    }


    @Test
    void shouldGetItemsWhenUseSearch() throws Exception {
        when(itemService.search(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(item1Output));

        this.mockMvc.perform(get("/items/search")
                        .param("text", "АккуМ")
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(item1Output.getId()))
                .andExpect(jsonPath("$[0].name").value(item1Output.getName()))
                .andExpect(jsonPath("$[0].description").value(item1Output.getDescription()))
                .andExpect(jsonPath("$[0].ownerId").value(item1Output.getOwnerId()));
    }

    @Test
    void shouldAddComment() throws Exception {
        when(itemService.addComment(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(CommentDto.class)))
                .thenReturn(commentDto);

        this.mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .content(asJsonString(commentDto))
                        .header(USER_ID_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.authorName").value(commentDto.getAuthorName()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.itemId").value(commentDto.getItemId()))
                .andExpect(jsonPath("$.created").value(commentDto.getCreated().toString()));
    }

    public String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}