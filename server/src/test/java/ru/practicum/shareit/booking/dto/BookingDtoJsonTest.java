package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> jsonBookingDto;
    @Autowired
    private JacksonTester<BookingDtoOutput> jsonBookingDtoOutput;

    @Test
    void shouldSerializationBookingDto() throws IOException {
        JsonContent<BookingDto> result = jsonBookingDto.write(BookingDto.builder()
                .id(1L)
                .itemId(1L)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().withNano(0).plusDays(1))
                .end(LocalDateTime.now().withNano(0).plusDays(2))
                .build());

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(LocalDateTime.now().withNano(0).plusDays(1).toString());
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(LocalDateTime.now().withNano(0).plusDays(2).toString());
    }

    @Test
    void shouldSerializationBookingDtoOutput() throws IOException {
        JsonContent<BookingDtoOutput> result = jsonBookingDtoOutput.write(BookingDtoOutput.builder()
                .id(1L)
                .booker(UserDto.builder()
                        .id(1L)
                        .name("name")
                        .email("aaa@mail.ru")
                        .build())
                .item(ItemDto.builder()
                        .id(1L)
                        .name("name")
                        .description("desc")
                        .requestId(1L)
                        .ownerId(1L)
                        .build())
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().withNano(0).plusDays(1).withNano(0))
                .end(LocalDateTime.now().withNano(0).plusDays(2).withNano(0))
                .build());

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("name");
        assertThat(result).extractingJsonPathStringValue("$.booker.email").isEqualTo("aaa@mail.ru");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("name");
        assertThat(result).extractingJsonPathStringValue("$.item.description").isEqualTo("desc");
        assertThat(result).extractingJsonPathNumberValue("$.item.ownerId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.item.requestId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(LocalDateTime.now().withNano(0).plusDays(1).toString());
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(LocalDateTime.now().withNano(0).plusDays(2).toString());
    }
}