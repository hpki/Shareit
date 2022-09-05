package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    public UserServiceImpl(UserStorage userRepository) {
        this.userStorage = userRepository;
    }

    @Override
    public List<User> getAll() {
        return userStorage.findAll();
    }

    @Override
    public User getUserById(long id) throws NoSuchElementException {
        if (userStorage.findById(id).isEmpty()) {
            throw new NoSuchElementException("Пользователь с таким id не найден!");
        } else {
            return userStorage.findById(id).get();
        }
    }

    @Override
    public User addUser(UserDto userDto) throws IllegalArgumentException {
        User user = UserMapper.toUser(userDto);
        return userStorage.save(user);
    }

    @Override
    public User updateUser(long id, User user) throws NoSuchElementException, IllegalArgumentException {
        Optional<User> oldUserOpt = userStorage.findById(id);
        if (oldUserOpt.isEmpty()) {
            throw new NoSuchElementException("Пользователь не найден!");
        }
        User oldUser = oldUserOpt.get();
        User newUser = new User(id, user.getName() == null ? oldUser.getName() : user.getName(),
                user.getEmail() == null ? oldUser.getEmail() : user.getEmail());
        return userStorage.save(newUser);
    }

    @Override
    public void deleteUserById(long id) {
        userStorage.deleteById(id);
    }
}