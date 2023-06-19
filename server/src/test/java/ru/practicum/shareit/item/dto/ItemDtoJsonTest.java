package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class ItemDtoJsonTest {
    @Autowired
    private JacksonTester<ItemDto> jsonItemDto;
    @Autowired
    private JacksonTester<ItemDtoByOwner> jsonItemDtoByOwner;


    @Test
    void shouldSerializationItemDto() throws IOException {

        JsonContent<ItemDto> result = jsonItemDto.write(ItemDto.builder()
                .id(1L)
                .ownerId(1L)
                .available(true)
                .description("desc")
                .name("name")
                .requestId(1L)
                .build());

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("name");
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(1);
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("desc");
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);
    }

    @Test
    void shouldSerializationItemDtoByOwner() throws IOException {

        JsonContent<ItemDtoByOwner> result = jsonItemDtoByOwner.write(
                ItemDtoByOwner.builder()
                .id(1L)
                .name("name")
                .nextBooking(BookingDto.builder()
                        .id(1L)
                        .itemId(1L)
                            .start(LocalDateTime.now().withNano(0).plusDays(1))
                        .end(LocalDateTime.now().withNano(0).plusDays(2))
                        .status(BookingStatus.APPROVED)
                        .build())
                .lastBooking(BookingDto.builder()
                        .id(1L)
                        .itemId(1L)
                        .start(LocalDateTime.now().withNano(0).minusDays(2))
                        .end(LocalDateTime.now().withNano(0).minusDays(1))
                        .status(BookingStatus.APPROVED)
                        .build())
                .requestId(1L)
                .description("desc")
                .available(true)
                .build());

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("name");
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.start")
                .isEqualTo(LocalDateTime.now().plusDays(1).withNano(0).toString());
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.end")
                .isEqualTo(LocalDateTime.now().plusDays(2).withNano(0).toString());
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.status")
                .isEqualTo("APPROVED");
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id")
                .isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.start")
                .isEqualTo(LocalDateTime.now().minusDays(2).withNano(0).toString());
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.end")
                .isEqualTo(LocalDateTime.now().minusDays(1).withNano(0).toString());
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.status")
                .isEqualTo("APPROVED");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("desc");
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);
    }

}