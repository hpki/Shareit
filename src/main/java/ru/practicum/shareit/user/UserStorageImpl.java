package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.ExistsException;

import java.util.*;

@Repository
public class UserStorageImpl implements UserStorage {
    private static final String EMAIL_VALIDATION =
            "^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$";
    private final Map<String, User> users = new HashMap<>();
    private long id = 0;

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(long id) throws NoSuchElementException {
        User needUser = null;
        for (User user : users.values()) {
            if (user.getId() == id) {
                needUser = user;
                break;
            }
        }
        if (needUser == null) {
            throw new NoSuchElementException("Пользователь не найден");
        }
        return needUser;
    }

    @Override
    public User addUser(User user) throws ExistsException, IllegalArgumentException {
        if (user.getEmail() == null) {
            throw new IllegalArgumentException("Пустой e-mail не допускается");
        }
        isValidUser(user);
        if (user.getId() == 0 || users.get(user.getEmail()).getId() != user.getId()) {
            id++;
            user.setId(id);
            users.put(user.getEmail(), user);
            return user;
        } else {
            throw new ExistsException("Пользователь уже существует");
        }
    }

    @Override
    public User updateUser(long id, User user) throws NoSuchElementException, ExistsException,
            IllegalArgumentException {
        isValidUser(user);
        User oldUser = null;
        for (User cashedUser : users.values()) {
            if (cashedUser.getId() == id) {
                oldUser = cashedUser;
                break;
            }
        }
        if (oldUser == null) {
            throw new NoSuchElementException("Пользователь не найден");
        }
        User newUser = new User(id, user.getName() == null ? oldUser.getName() : user.getName(),
                user.getEmail() == null ? oldUser.getEmail() : user.getEmail());
        users.remove(oldUser.getEmail());
        users.put(newUser.getEmail(), newUser);
        return newUser;
    }

    @Override
    public void deleteUserById(long id) {
        User needUser = null;
        for (User user : users.values()) {
            if (user.getId() == id) {
                needUser = user;
                break;
            }
        }
        if (needUser != null) {
            users.remove(needUser.getEmail());
        }
    }

    private void isValidUser(User user) throws ExistsException, IllegalArgumentException {
        if (users.containsKey(user.getEmail())) {
            throw new ExistsException("Пользователь с таким email уже существует");
        }
        if (user.getEmail() != null && !user.getEmail().toLowerCase().matches(EMAIL_VALIDATION)) {
            throw new IllegalArgumentException("Недопустимый email");
        }
    }
}
