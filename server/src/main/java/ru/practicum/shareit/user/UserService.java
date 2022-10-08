package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();

    UserDto getUser(long userId);

    UserDto addUser(UserDto userDto);

    UserDto editUser(long userId, UserDto userDto);

    void deleteUser(long userId);
}
