package ru.practicum.shareit.exception;

public class WrongStartOrEndTimeException extends RuntimeException {
    public WrongStartOrEndTimeException(String message) {
        super(message);
    }
}
