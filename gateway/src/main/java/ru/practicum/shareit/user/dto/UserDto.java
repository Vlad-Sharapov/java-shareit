package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.utils.Marker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    @NotBlank(groups = Marker.OnCreate.class)
    private String name;

    @NotBlank(groups = Marker.OnCreate.class, message = "email can not be null")
    @Email(groups = {Marker.OnCreate.class, Marker.OnUpdate.class}, message = "incorrect email has been entered")
    private String email;
}
