package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class ItemRequestJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> jsonItemRequestDto;

    @Test
    void shouldSerializationItemRequestDto() throws IOException {
        JsonContent<ItemRequestDto> result = jsonItemRequestDto.write(ItemRequestDto.builder()
                .id(1L)
                .requestorId(1L)
                .created(LocalDateTime.now().withNano(0))
                .description("desc")
                .items(List.of(ItemDto.builder()
                        .id(1L)
                        .name("name")
                        .description("itemDesc")
                        .requestId(1L)
                        .available(true)
                        .ownerId(1L)
                        .build()))
                .build());

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.requestorId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(LocalDateTime.now().withNano(0).toString());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("desc");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("name");
        assertThat(result).extractingJsonPathStringValue("$.items[0].description")
                .isEqualTo("itemDesc");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].requestId").isEqualTo(1);
        assertThat(result).extractingJsonPathBooleanValue("$.items[0].available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(1);
    }
}