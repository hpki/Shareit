package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ExistsException;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserServiceImpl implements UserService {
    private UserStorage userStorage;

    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public List<User> getAll() {
        return userStorage.getAll();
    }

    @Override
    public User getUserById(long id) throws NoSuchElementException {
        return userStorage.getUserById(id);
    }

    @Override
    public User addUser(User user) throws ExistsException {
        return userStorage.addUser(user);
    }

    @Override
    public User updateUser(long id, User user) throws NoSuchElementException, ExistsException, IllegalArgumentException {
        return userStorage.updateUser(id, user);
    }

    @Override
    public void deleteUserById(long id) {
        userStorage.deleteUserById(id);
    }
}