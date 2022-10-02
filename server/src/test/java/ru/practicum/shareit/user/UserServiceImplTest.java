package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private UserServiceImpl userService;

    private User user = new User(1, "testName", "testEmail@yandex.ru");
    private User userUpdated = new User(1, "testNameUpdated", "testEmailUpdated@yandex.ru");

    @Test
    void getAllUsers() {
        Mockito.when(userStorage.findAll()).thenReturn(List.of(user));
        Assertions.assertEquals(List.of(UserMapper.toUserDto(user)), userService.getAllUsers());
    }

    @Test
    void getUser() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        Assertions.assertEquals(UserMapper.toUserDto(user), userService.getUser(user.getId()));
    }

    @Test
    void createUser() {
        Mockito.when(userStorage.save(any())).thenReturn(user);
        Assertions.assertEquals(UserMapper.toUserDto(user), userService.addUser(UserMapper.toUserDto(user)));
    }

    @Test
    void updateUser() {
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(user));
        Mockito.when(userStorage.save(any())).thenReturn(userUpdated);
        Assertions.assertEquals(UserMapper.toUserDto(userUpdated), userService.editUser(user.getId(),
                UserMapper.toUserDto(user)));
    }

    @Test
    void deleteUser() {
        userService.deleteUser(user.getId());
        Mockito.verify(userStorage, Mockito.times(1)).deleteById(user.getId());
    }
}
