package ru.practicum.shareit.exceptions;

public class WrongTimeException extends RuntimeException {
    public WrongTimeException(String message) {
        super(message);
    }
}
