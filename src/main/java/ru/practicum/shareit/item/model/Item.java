package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder(toBuilder = true)
public class Item {

    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long owner;
    private Long request;

}
