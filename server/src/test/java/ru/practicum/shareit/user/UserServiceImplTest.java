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
    UserRepository userRepository;

    @InjectMocks
    UserServiceImpl userService;

    User user = new User(1, "testName", "testEmail@yandex.ru");
    User userUpdated = new User(1, "testNameUpdated", "testEmailUpdated@yandex.ru");

    @Test
    void getAllUsers() {
        Mockito.when(userRepository.findAll()).thenReturn(List.of(user));
        Assertions.assertEquals(List.of(UserMapper.toUserDto(user)), userService.getAllUsers());
    }

    @Test
    void getUser() {
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        Assertions.assertEquals(UserMapper.toUserDto(user), userService.getUser(user.getId()));
    }

    @Test
    void createUser() {
        Mockito.when(userRepository.save(any())).thenReturn(user);
        Assertions.assertEquals(UserMapper.toUserDto(user), userService.createUser(UserMapper.toUserDto(user)));
    }

    @Test
    void updateUser() {
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any())).thenReturn(userUpdated);
        Assertions.assertEquals(UserMapper.toUserDto(userUpdated), userService.updateUser(user.getId(), UserMapper.toUserDto(user)));
    }

    @Test
    void deleteUser() {
        userService.deleteUser(user.getId());
        Mockito.verify(userRepository, Mockito.times(1)).deleteById(user.getId());
    }
}
