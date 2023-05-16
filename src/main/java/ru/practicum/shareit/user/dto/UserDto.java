package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.auxilary.Marker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


@Data
@Builder(toBuilder = true)
public class UserDto {
    private Long id;
    @NotBlank(groups = Marker.OnCreate.class)
    private String name;
    @NotBlank(groups = Marker.OnCreate.class)
    @Email(groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String email;
}
