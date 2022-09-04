package ru.practicum.shareit.exeptions;

public class BookingUnsupportedTypeException extends Exception {
    public BookingUnsupportedTypeException(String message) {
        super(message);
    }
}
