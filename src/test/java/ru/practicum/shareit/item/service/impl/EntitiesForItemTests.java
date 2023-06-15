package ru.practicum.shareit.item.service.impl;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.booking.enums.BookingStatus.WAITING;

public class EntitiesForItemTests {


    protected List<User> users = List.of(User.builder().id(1L).name("user1").email("newuser1@mail.ru").build(),
            User.builder().id(2L).name("user2").email("newuser2@mail.ru").build());
    protected ItemRequest itemRequest = ItemRequest.builder()
            .id(1L)
            .description("Учебник китайского")
            .requestor(users.get(0))
            .created(LocalDateTime.now())
            .build();

    protected List<Item> items = List.of(Item.builder()
                    .id(1L)
                    .name("Коркуниан А.Ф. Практический курс китайского языка. 12-е изд. в двух томах")
                    .description("Учебник по Китайскому языку")
                    .available(true)
                    .owner(users.get(1))
                    .request(itemRequest)
                    .build(),
            Item.builder()
                    .id(2L)
                    .name("Учебник по линейной алгебре")
                    .description("Учебник в котором изложены такие темы как: частные производные, матрицы, векторный анализ и др.")
                    .available(false)
                    .owner(users.get(1))
                    .request(itemRequest)
                    .build());
    protected List<Booking> bookings = List.of(Booking.builder()
                    .id(1L)
                    .item(items.get(0))
                    .booker(users.get(0))
                    .start(LocalDateTime.now().plusDays(1))
                    .end(LocalDateTime.now().plusDays(2))
                    .status(WAITING)
                    .build(),
            Booking.builder()
                    .id(2L)
                    .item(items.get(0))
                    .booker(users.get(0))
                    .start(LocalDateTime.now().minusDays(3))
                    .end(LocalDateTime.now().minusDays(2))
                    .status(WAITING)
                    .build());


    protected User user1 = User.builder().id(1L).name("user1").email("newuser1@mail.ru").build();
    protected User user2 = User.builder().id(2L).name("user2").email("newuser2@mail.ru").build();

    protected ItemRequest itemRequestUser1 = ItemRequest.builder()
            .id(1L)
            .description("Учебник китайского")
            .requestor(user1)
            .created(LocalDateTime.now().withNano(0))
            .build();

    protected ItemDto itemDto = ItemDto.builder()
            .name("Коркуниан А.Ф. Практический курс китайского языка. 12-е изд. в двух томах")
            .description("Учебник по Китайскому языку")
            .available(true)
            .requestId(itemRequestUser1.getId())
            .build();

    protected Comment comment = Comment.builder()
            .id(1L)
            .author(user1)
            .created(LocalDateTime.now().withNano(0))
            .text("comment1")
            .item(items.get(0))
            .build();

}
