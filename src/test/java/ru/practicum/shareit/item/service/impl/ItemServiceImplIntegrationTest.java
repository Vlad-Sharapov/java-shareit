package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplIntegrationTest extends EntitiesForItemTests {

    private final ItemService itemService;
    private final EntityManager em;


    @BeforeEach
    public void beforeEach() {

        users = List.of(
                user2.toBuilder()
                        .id(null)
                        .build(),

                user1.toBuilder()
                        .id(null)
                        .build()
        );

        itemRequest = itemRequestUser1.toBuilder()
                .id(null)
                .requestor(users.get(1))
                .build();


        items = List.of(
                items.get(0).toBuilder()
                        .id(null)
                        .request(itemRequest)
                        .owner(users.get(0))
                        .build(),
                items.get(1).toBuilder()
                        .id(null)
                        .request(null)
                        .owner(users.get(0))
                        .build()
        );


        bookings = List.of(
                bookings.get(0).toBuilder()
                        .id(null)
                        .booker(users.get(1))
                        .item(items.get(0))
                        .build(),
                bookings.get(1).toBuilder()
                        .id(null)
                        .booker(users.get(1))
                        .item(items.get(0))
                        .build()
        );

        for (User user : users) {
            em.persist(user);
        }
        em.flush();

        em.persist(itemRequest);
        em.flush();

        for (Item item : items) {
            em.persist(item);
        }
        em.flush();

        for (Booking booking : bookings) {
            em.persist(booking);
        }
        em.flush();

        em.persist(comment.toBuilder()
                .id(null)
                .author(users.get(1))
                .item(items.get(0))
                .build());
        em.flush();
    }

    @Test
    void shouldGetUserItemsWithRequestAndComments() {
        List<ItemDtoByOwner> userItems = itemService.getUserItems(users.get(0).getId(), 0, 5);

        assertThat(userItems, hasSize(2));
        Item checkedEntity = items.get(0);
        Booking nextBooking = bookings.get(0);
        Booking lastBooking = bookings.get(1);
        assertThat(userItems.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(checkedEntity.getName())),
                hasProperty("description", equalTo(checkedEntity.getDescription())),
                hasProperty("available", equalTo(checkedEntity.getAvailable())),
                hasProperty("requestId", equalTo(checkedEntity.getRequest().getId())),
                hasProperty("lastBooking", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("start", equalTo(lastBooking.getStart())),
                        hasProperty("end", equalTo(lastBooking.getEnd())),
                        hasProperty("itemId", equalTo(lastBooking.getItem().getId())),
                        hasProperty("bookerId", equalTo(lastBooking.getBooker().getId())),
                        hasProperty("status", equalTo(lastBooking.getStatus())
                        ))),
                hasProperty("nextBooking", allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("start", equalTo(nextBooking.getStart())),
                        hasProperty("end", equalTo(nextBooking.getEnd())),
                        hasProperty("itemId", equalTo(nextBooking.getItem().getId())),
                        hasProperty("bookerId", equalTo(nextBooking.getBooker().getId())),
                        hasProperty("status", equalTo(nextBooking.getStatus())
                        ))),
                hasProperty("comments", hasItem(allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("text", equalTo(comment.getText())),
                        hasProperty("itemId", equalTo(checkedEntity.getId())),
                        hasProperty("authorName", equalTo(comment.getAuthor().getName())),
                        hasProperty("created", equalTo(comment.getCreated()))
                )))
        ));
    }

    @Test
    void shouldGetUserItemsWithUserDoesntHaveEntity() {
        List<ItemDtoByOwner> userItems = itemService.getUserItems(users.get(1).getId(), 0, 5);

        assertThat(userItems, hasSize(0));

    }

    @Test
    void shouldSearchAllItemsByTextContent() {

        List<ItemDto> itemsDto = itemService.search("УчеБ", 0, 5);
        Item item = items.get(0);

        assertThat(itemsDto, hasSize(1));
        assertThat(itemsDto.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(item.getName())),
                hasProperty("description", equalTo(item.getDescription())),
                hasProperty("available", equalTo(item.getAvailable()))
        ));
    }

    @Test
    void shouldSearchOneItemByTextContent() {

        List<ItemDto> itemsDto = itemService.search("КиТай", 0, 5);

        assertThat(itemsDto, hasSize(1));
        assertThat(itemsDto, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(items.get(0).getName())),
                hasProperty("description", equalTo(items.get(0).getDescription())),
                hasProperty("available", equalTo(items.get(0).getAvailable()))
        )));
    }

    @Test
    void shouldSearchNoneItemByTextContent() {
        List<ItemDto> itemsDto = itemService.search("qwert", 0, 5);

        assertThat(itemsDto, hasSize(0));
    }
}