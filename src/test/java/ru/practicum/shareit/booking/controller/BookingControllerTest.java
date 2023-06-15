package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOutput;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
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

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @MockBean
    private BookingService bookingService;

    @Autowired
    private
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    UserDto user1 = UserDto.builder().name("user1").email("user1@mail.ru").build();

    ItemDto item1 = ItemDto.builder()
            .name("Дрель")
            .description("Обычная дрель")
            .ownerId(user1.getId())
            .available(true)
            .build();

    BookingDtoOutput bookingDto1 = BookingDtoOutput.builder()
            .id(1L)
            .item(item1)
            .booker(user1)
            .start(LocalDateTime.now().plusDays(1).withNano(0))
            .end(LocalDateTime.now().plusDays(2).withNano(0))
            .status(APPROVED)
            .build();

    @Test
    void add() throws Exception {
        when(bookingService.add(Mockito.anyLong(), Mockito.any(BookingDto.class)))
                .thenReturn(bookingDto1);

        this.mockMvc.perform(post("/bookings")
                        .content(asJsonString(bookingDto1))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto1.getId()))
                .andExpect(jsonPath("$.item.id").value(bookingDto1.getItem().getId()))
                .andExpect(jsonPath("$.item.name").value(bookingDto1.getItem().getName()))
                .andExpect(jsonPath("$.item.description").value(bookingDto1.getItem().getDescription()))
                .andExpect(jsonPath("$.item.ownerId").value(bookingDto1.getItem().getOwnerId()))
                .andExpect(jsonPath("$.item.available").value(bookingDto1.getItem().getAvailable()))
                .andExpect(jsonPath("$.booker.id").value(bookingDto1.getBooker().getId()))
                .andExpect(jsonPath("$.booker.email").value(bookingDto1.getBooker().getEmail()))
                .andExpect(jsonPath("$.booker.name").value(bookingDto1.getBooker().getName()))
                .andExpect(jsonPath("$.start").value(bookingDto1.getStart().toString()))
                .andExpect(jsonPath("$.end").value(bookingDto1.getEnd().toString()))
                .andExpect(jsonPath("$.status").value(bookingDto1.getStatus().toString()));
    }

    @Test
    void shouldStatus404WhenAddThrowsEntityNotFoundException() throws Exception {
        when(bookingService.add(Mockito.anyLong(), Mockito.any(BookingDto.class)))
                .thenThrow(new EntityNotFoundException("User not found."));

        this.mockMvc.perform(post("/bookings")
                        .content(asJsonString(bookingDto1))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldStatus400WhenAddThrowsBadRequestException() throws Exception {
        when(bookingService.add(Mockito.anyLong(), Mockito.any(BookingDto.class)))
                .thenThrow(new BadRequestException("Bad request"));

        this.mockMvc.perform(post("/bookings")
                        .content(asJsonString(bookingDto1))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBookingBeApproved() throws Exception {
        when(bookingService.approved(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean()))
                .thenReturn(bookingDto1.toBuilder()
                        .status(APPROVED)
                        .build());

        this.mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto1.getId()))
                .andExpect(jsonPath("$.item.id").value(bookingDto1.getItem().getId()))
                .andExpect(jsonPath("$.item.name").value(bookingDto1.getItem().getName()))
                .andExpect(jsonPath("$.item.description").value(bookingDto1.getItem().getDescription()))
                .andExpect(jsonPath("$.item.ownerId").value(bookingDto1.getItem().getOwnerId()))
                .andExpect(jsonPath("$.item.available").value(bookingDto1.getItem().getAvailable()))
                .andExpect(jsonPath("$.booker.id").value(bookingDto1.getBooker().getId()))
                .andExpect(jsonPath("$.booker.email").value(bookingDto1.getBooker().getEmail()))
                .andExpect(jsonPath("$.booker.name").value(bookingDto1.getBooker().getName()))
                .andExpect(jsonPath("$.start").value(bookingDto1.getStart().toString()))
                .andExpect(jsonPath("$.end").value(bookingDto1.getEnd().toString()))
                .andExpect(jsonPath("$.status").value(bookingDto1.getStatus().toString()));

    }

    @Test
    void shouldStatus404WhenApprovedThrowsEntityNotFoundException() throws Exception {
        when(bookingService.approved(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean()))
                .thenThrow(new EntityNotFoundException("User not found."));

        this.mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldStatus400WhenApprovedThrowsBadRequestException() throws Exception {
        when(bookingService.approved(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean()))
                .thenThrow(new BadRequestException("Bad request"));

        this.mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetBookingById() throws Exception {
        when(bookingService.getBooking(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(bookingDto1.toBuilder()
                        .status(APPROVED)
                        .build());

        this.mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto1.getId()))
                .andExpect(jsonPath("$.item.id").value(bookingDto1.getItem().getId()))
                .andExpect(jsonPath("$.item.name").value(bookingDto1.getItem().getName()))
                .andExpect(jsonPath("$.item.description").value(bookingDto1.getItem().getDescription()))
                .andExpect(jsonPath("$.item.ownerId").value(bookingDto1.getItem().getOwnerId()))
                .andExpect(jsonPath("$.item.available").value(bookingDto1.getItem().getAvailable()))
                .andExpect(jsonPath("$.booker.id").value(bookingDto1.getBooker().getId()))
                .andExpect(jsonPath("$.booker.email").value(bookingDto1.getBooker().getEmail()))
                .andExpect(jsonPath("$.booker.name").value(bookingDto1.getBooker().getName()))
                .andExpect(jsonPath("$.start").value(bookingDto1.getStart().toString()))
                .andExpect(jsonPath("$.end").value(bookingDto1.getEnd().toString()))
                .andExpect(jsonPath("$.status").value(bookingDto1.getStatus().toString()));

    }

    @Test
    void shouldStatus404WhenBookingThrowsEntityNotFoundException() throws Exception {
        when(bookingService.getBooking(Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException("User not found."));

        this.mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllUserBookings() throws Exception {
        when(bookingService.getAllUserBookings(Mockito.anyLong(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(bookingDto1));

        this.mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(bookingDto1.getId()))
                .andExpect(jsonPath("$[0].item.id").value(bookingDto1.getItem().getId()))
                .andExpect(jsonPath("$[0].item.name").value(bookingDto1.getItem().getName()))
                .andExpect(jsonPath("$[0].item.description").value(bookingDto1.getItem().getDescription()))
                .andExpect(jsonPath("$[0].item.ownerId").value(bookingDto1.getItem().getOwnerId()))
                .andExpect(jsonPath("$[0].item.available").value(bookingDto1.getItem().getAvailable()))
                .andExpect(jsonPath("$[0].booker.id").value(bookingDto1.getBooker().getId()))
                .andExpect(jsonPath("$[0].booker.email").value(bookingDto1.getBooker().getEmail()))
                .andExpect(jsonPath("$[0].booker.name").value(bookingDto1.getBooker().getName()))
                .andExpect(jsonPath("$[0].start").value(bookingDto1.getStart().toString()))
                .andExpect(jsonPath("$[0].end").value(bookingDto1.getEnd().toString()))
                .andExpect(jsonPath("$[0].status").value(bookingDto1.getStatus().toString()));
    }

    @Test
    void shouldStatus404WhenGetAllUserBookingsThrowsEntityNotFoundException() throws Exception {
        when(bookingService.getAllUserBookings(Mockito.anyLong(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                .thenThrow(new EntityNotFoundException("User not found."));

        this.mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllOwnerBookings() throws Exception {
        when(bookingService.getAllOwnerBookings(Mockito.anyLong(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(bookingDto1));

        this.mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(bookingDto1.getId()))
                .andExpect(jsonPath("$[0].item.id").value(bookingDto1.getItem().getId()))
                .andExpect(jsonPath("$[0].item.name").value(bookingDto1.getItem().getName()))
                .andExpect(jsonPath("$[0].item.description").value(bookingDto1.getItem().getDescription()))
                .andExpect(jsonPath("$[0].item.ownerId").value(bookingDto1.getItem().getOwnerId()))
                .andExpect(jsonPath("$[0].item.available").value(bookingDto1.getItem().getAvailable()))
                .andExpect(jsonPath("$[0].booker.id").value(bookingDto1.getBooker().getId()))
                .andExpect(jsonPath("$[0].booker.email").value(bookingDto1.getBooker().getEmail()))
                .andExpect(jsonPath("$[0].booker.name").value(bookingDto1.getBooker().getName()))
                .andExpect(jsonPath("$[0].start").value(bookingDto1.getStart().toString()))
                .andExpect(jsonPath("$[0].end").value(bookingDto1.getEnd().toString()))
                .andExpect(jsonPath("$[0].status").value(bookingDto1.getStatus().toString()));
    }

    @Test
    void shouldStatus404WhenGetAllOwnerBookingsThrowsEntityNotFoundException() throws Exception {
        when(bookingService.getAllOwnerBookings(Mockito.anyLong(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                .thenThrow(new EntityNotFoundException("User not found."));

        this.mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .accept("*/*"))
                .andExpect(status().isNotFound());
    }

    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}