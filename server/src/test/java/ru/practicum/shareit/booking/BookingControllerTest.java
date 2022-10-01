package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    User userOwner = new User(1, "testOwnerName", "testOwnerEmail@yandex.ru");
    User userBooker = new User(2, "testBookerName", "testBookerEmail@yandex.ru");
    Item item = new Item(1, "testName", "testDescription", true, userOwner, null);
    Booking booking = new Booking(1, LocalDateTime.of(2023, Month.APRIL, 2, 12, 12, 12),
            LocalDateTime.of(2024, Month.APRIL, 2, 12, 12, 12), item, userBooker, Status.WAITING);
    Booking bookingApproved = new Booking(1, LocalDateTime.of(2023, Month.APRIL, 2, 12, 12, 12),
            LocalDateTime.of(2024, Month.APRIL, 2, 12, 12, 12), item, userBooker, Status.APPROVED);
    BookingDto bookingDto = new BookingDto(1, 1, 2,
            LocalDateTime.of(2023, Month.APRIL, 2, 12, 12, 12),
            LocalDateTime.of(2024, Month.APRIL, 2, 12, 12, 12));


    @Test
    void saveNewBooking() throws Exception {
        when(bookingService.createBooking(anyLong(), any())).thenReturn(booking);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(booking.getStart().toString())))
                .andExpect(jsonPath("$.end", is(booking.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is((int) item.getId())))
                .andExpect(jsonPath("$.booker.id", is((int) userBooker.getId())))
                .andExpect(jsonPath("$.status", is(Status.WAITING.toString())));
    }

    @Test
    void updateExistingBooking() throws Exception {
        when(bookingService.updateBooking(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingApproved);
        mvc.perform(patch("/bookings/{bookingId}", String.valueOf(booking.getId()))
                        .param("approved", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingApproved.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingApproved.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingApproved.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is((int) item.getId())))
                .andExpect(jsonPath("$.booker.id", is((int) userBooker.getId())))
                .andExpect(jsonPath("$.status", is(Status.APPROVED.toString())));
    }

    @Test
    void getBookingById() throws Exception {
        when(bookingService.getBooking(anyLong(), anyLong())).thenReturn(booking);
        mvc.perform(get("/bookings/{bookingId}", String.valueOf(booking.getId()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(booking.getStart().toString())))
                .andExpect(jsonPath("$.end", is(booking.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is((int) item.getId())))
                .andExpect(jsonPath("$.booker.id", is((int) userBooker.getId())))
                .andExpect(jsonPath("$.status", is(Status.WAITING.toString())));
    }

    @Test
    void getAllBookings() throws Exception {
        when(bookingService.getAllBookings(anyLong(), anyString(), any())).thenReturn(List.of(booking));
        mvc.perform(get("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", is(booking.getStart().toString())))
                .andExpect(jsonPath("$.[0].end", is(booking.getEnd().toString())))
                .andExpect(jsonPath("$.[0].item.id", is((int) item.getId())))
                .andExpect(jsonPath("$.[0].booker.id", is((int) userBooker.getId())))
                .andExpect(jsonPath("$.[0].status", is(Status.WAITING.toString())));
    }

    @Test
    void getAllBookingsForOwner() throws Exception {
        when(bookingService.getAllBookingsForOwner(anyLong(), anyString(), any())).thenReturn(List.of(booking));
        mvc.perform(get("/bookings/owner")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", is(booking.getStart().toString())))
                .andExpect(jsonPath("$.[0].end", is(booking.getEnd().toString())))
                .andExpect(jsonPath("$.[0].item.id", is((int) item.getId())))
                .andExpect(jsonPath("$.[0].booker.id", is((int) userBooker.getId())))
                .andExpect(jsonPath("$.[0].status", is(Status.WAITING.toString())));
    }
}

