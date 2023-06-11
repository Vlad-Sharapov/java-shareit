package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.utils.Marker;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private Long id;
    @NotBlank(groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String name;
    @NotBlank(groups = Marker.OnCreate.class)
    private String description;
    @NotNull(groups = Marker.OnCreate.class)
    private Boolean available;
    private Long request;
}

