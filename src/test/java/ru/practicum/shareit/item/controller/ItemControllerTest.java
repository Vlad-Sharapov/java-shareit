package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    ItemDto item1 = ItemDto.builder().name("Дрель").description("Обычная дрель").available(true).build();
    ItemDto itemWithOutAvailable = ItemDto.builder().name("Дрель").description("Обычная дрель").build();
    ItemDto itemWithEmptyName = ItemDto.builder().name("Дрель").description("Обычная дрель").build();
    ItemDto itemWithEmptyDesc = ItemDto.builder().name("Дрель").description("Обычная дрель").build();
    ItemDto updateItem1 = ItemDto.builder().name("Дрель+").description("Аккумуляторная дрель").available(false).build();
    ItemDto updateAvailableForItem1 = ItemDto.builder().available(true).build();
    ItemDto updateDescForItem1 = ItemDto.builder().description("Аккумуляторная дрель + аккумулятор").build();
    ItemDto updateNameForItem1 = ItemDto.builder().name("Аккумуляторная дрель").build();
    ItemDto item2 = ItemDto.builder().name("Отвертка").description("Обычная Отвертка").available(true).build();
    ItemDto item3 = ItemDto.builder().name("Болгарка").description("Болгарка макита").available(true).build();



    @Test
    void shouldItemCreateWhenUsePostItems() throws Exception {
        this.mockMvc.perform(post("/items")
                        .content(asJsonString(item1)).contentType("application/json")
                        .header("X-Sharer-User-Id", 1L)
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void shouldStatus400WhenUsePostItemsWithoutXSharerUserId() throws Exception {
        this.mockMvc.perform(post("/items")
                        .content(asJsonString(item1)).contentType("application/json")
                        .accept("*/*"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldStatus400WhenUsePostItemsWithNotFoundUser() throws Exception {
        this.mockMvc.perform(post("/items")
                        .content(asJsonString(item1)).contentType("application/json")
                        .header("X-Sharer-User-Id", 111L)
                        .accept("*/*"))
                .andExpect(status().isNotFound());

    }

    @Test
    void shouldStatus400WhenUsePostItemsWithoutAvailable() throws Exception {
        this.mockMvc.perform(post("/items")
                        .content(asJsonString(itemWithOutAvailable)).contentType("application/json")
                        .header("X-Sharer-User-Id", 1L)
                        .accept("*/*"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldStatus400WhenUsePostItemsWithEmptyName() throws Exception {
        this.mockMvc.perform(post("/items")
                        .content(asJsonString(itemWithEmptyName)).contentType("application/json")
                        .header("X-Sharer-User-Id", 1L)
                        .accept("*/*"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldStatus400WhenUsePostItemsWithEmptyDescription() throws Exception {
        this.mockMvc.perform(post("/items")
                        .content(asJsonString(itemWithEmptyDesc)).contentType("application/json")
                        .header("X-Sharer-User-Id", 1L)
                        .accept("*/*"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldItemUpdateWhenUsePatchItems() throws Exception {
        this.mockMvc.perform(patch("/items/{itemId}", 1)
                        .content(asJsonString(updateItem1)).contentType("application/json")
                        .header("X-Sharer-User-Id", 1L)
                        .accept("*/*"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldItemUpdateStatus400WhenUsePatchItemsWithoutHeader() throws Exception {
        this.mockMvc.perform(patch("/items/{itemId}", 1)
                        .content(asJsonString(updateItem1)).contentType("application/json")
                        .accept("*/*"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldItemUpdateStatus400WhenUsePatchItemsWithOtherUser() throws Exception {
        this.mockMvc.perform(patch("/items/{itemId}", 1)
                        .content(asJsonString(updateItem1)).contentType("application/json")
                        .header("X-Sharer-User-Id", 2L)
                        .accept("*/*"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldItemUpdateAvailableWhenUsePatchItems() throws Exception {
        this.mockMvc.perform(patch("/items/{itemId}", 1)
                        .content(asJsonString(updateAvailableForItem1)).contentType("application/json")
                        .header("X-Sharer-User-Id", 1L)
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void shouldItemUpdateNameWhenUsePatchItems() throws Exception {
        this.mockMvc.perform(patch("/items/{itemId}", 1)
                        .content(asJsonString(updateNameForItem1)).contentType("application/json")
                        .header("X-Sharer-User-Id", 1L)
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Аккумуляторная дрель"));

    }

    @Test
    void shouldItemUpdateDescriptionWhenUsePatchItems() throws Exception {
        this.mockMvc.perform(patch("/items/{itemId}", 1)
                        .content(asJsonString(updateDescForItem1)).contentType("application/json")
                        .header("X-Sharer-User-Id", 1L)
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Аккумуляторная дрель + аккумулятор"));
    }

    @Test
    void shouldGetItemWhenUseGetItemsWithParameterItemId() throws Exception {
        this.mockMvc.perform(get("/items/{itemId}", 1)
                        .content(asJsonString(item1)).contentType("application/json")
                        .header("X-Sharer-User-Id", 1L)
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldGetAllItemsForUserWhenUseGetItems() throws Exception {
        this.mockMvc.perform(post("/items")
                        .content(asJsonString(item2)).contentType("application/json")
                        .header("X-Sharer-User-Id", 1L)
                        .accept("*/*"))
                .andExpect(status().isOk());


        String contentAsString = this.mockMvc.perform(get("/items", 1)
                        .content(asJsonString(item1)).contentType("application/json")
                        .header("X-Sharer-User-Id", 1L)
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        List<ItemDto> itemDtos = objectMapper.readValue(contentAsString,
                new TypeReference<>() {
                });
        assertEquals(itemDtos.size(), 2);
        assertEquals(itemDtos.get(0).getId(), 1);
        assertEquals(itemDtos.get(1).getId(), 2);
    }


    @Test
    void shouldGetItemsWhenUseSearch() throws Exception {

        this.mockMvc.perform(post("/items")
                .content(asJsonString(item3)).contentType("application/json")
                .header("X-Sharer-User-Id", 2L)
                .accept("*/*"));

        String contentAsString = this.mockMvc.perform(get("/items/search")
                        .param("text", "БолГар")
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        List<ItemDto> itemDtos = objectMapper.readValue(contentAsString,
                new TypeReference<>() {
                });

        assertEquals(itemDtos.size(), 1);
        assertEquals(itemDtos.get(0).getName(), "Болгарка");

    }

    @Test
    void shouldGetItemsWhenUseSearchWithEmptyText() throws Exception {

        String contentAsString = this.mockMvc.perform(get("/items/search")
                        .param("text", "")
                        .accept("*/*"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        List<ItemDto> itemDtos = objectMapper.readValue(contentAsString,
                new TypeReference<>() {
                });

        assertEquals(itemDtos.size(), 0);


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