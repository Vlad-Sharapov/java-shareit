package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class CommentJsonTest {

    @Autowired
    private JacksonTester<CommentDto> JsonItemRequestDto;

    @Test
    void shouldSerializationItemRequestDto() throws IOException {
        JsonContent<CommentDto> result = JsonItemRequestDto.write(CommentDto.builder()
                .id(1L)
                .itemId(1L)
                .text("text")
                .created(LocalDateTime.now().withNano(0))
                .authorName("Vlad")
                .build());

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(LocalDateTime.now().withNano(0).toString());
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Vlad");
    }

}