package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.impl.EntitiesForItemTests;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
@AutoConfigureTestDatabase
class ItemRepositoryTest extends EntitiesForItemTests {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRepository itemRepository;

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

        users.forEach(em::persist);

        em.persist(itemRequest);

        items.forEach(em::persist);

        bookings.forEach(em::persist);

        em.persist(comment.toBuilder()
                .id(null)
                .author(users.get(1))
                .item(items.get(0))
                .build());
        em.flush();

    }

    @Test
    void shouldFindEntitiesByOwnerIdWhenUseFindByOwnerId() {
        List<Item> findEntities = itemRepository.findByOwnerId(users.get(0).getId());

        assertThat(findEntities, hasSize(2));
        for (Item item : items) {
            assertThat(findEntities, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(item.getName())),
                    hasProperty("description", equalTo(item.getDescription())),
                    hasProperty("available", equalTo(item.getAvailable()))
            )));
        }
    }

    @Test
    void shouldFindEntitiesByRequestIdInWhenUseFindByRequestIdIn() {
        Item testItem = items.get(0);
        List<Item> findEntities = itemRepository.findByRequestIdIn(List.of(itemRequest.getId()));

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(testItem.getName())),
                hasProperty("description", equalTo(testItem.getDescription())),
                hasProperty("available", equalTo(testItem.getAvailable()))
        ));
    }

    @Test
    void shouldFindEntitiesByRequestIdWhenUseFindByRequestId() {
        Item testItem = items.get(0);
        List<Item> findEntities = itemRepository.findByRequestId(itemRequest.getId());

        assertThat(findEntities, hasSize(1));
        assertThat(findEntities.get(0), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(testItem.getName())),
                hasProperty("description", equalTo(testItem.getDescription())),
                hasProperty("available", equalTo(testItem.getAvailable()))
        ));
    }

    @Test
    void shouldFindSearchEntitiesIdWhenUseFindAllByDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Item> findEntities =
                itemRepository.findAllByDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase("УчеБ",
                        "УчеБ", pageRequest);

        assertThat(findEntities, hasSize(2));
        for (Item item : items) {
            assertThat(findEntities, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(item.getName())),
                    hasProperty("description", equalTo(item.getDescription())),
                    hasProperty("available", equalTo(item.getAvailable()))
            )));
        }
    }
}