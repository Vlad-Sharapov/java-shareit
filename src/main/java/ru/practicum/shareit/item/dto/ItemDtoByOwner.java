package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoByOwner {

    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long request;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private List<CommentDto> comments;
}

