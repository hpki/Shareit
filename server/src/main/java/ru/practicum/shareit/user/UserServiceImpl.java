package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUser(long userId) {
        return UserMapper.toUserDto(userStorage.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!")));
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = userStorage.save(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto editUser(long userId, UserDto userDto) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!"));
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(userStorage.save(user));
    }

    @Override
    public void deleteUser(long userId) {
        userStorage.deleteById(userId);
    }
}
