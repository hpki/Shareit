package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.UserUpdate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDto {
    private long id;
    @NotNull
    @NotBlank
    private String name;
    @Email(groups = {UserUpdate.class, Default.class})
    @NotNull
    @NotBlank
    private String email;
}
