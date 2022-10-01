package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.Month;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=shareit-test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext
public class BookingServiceImplIntegrationTest {
    private final EntityManager em;
    private final BookingServiceImpl bookingService;

    @Test
    void createBooking() {
        em.createNativeQuery("insert into users (name, email) values (?,?)")
                .setParameter(1, "userOwnerName")
                .setParameter(2, "userOwnerEmail@mail.ru")
                .executeUpdate();
        em.createNativeQuery("insert into users (name, email) values (?,?)")
                .setParameter(1, "userBookerName")
                .setParameter(2, "userBookerEmail@mail.ru")
                .executeUpdate();
        em.createNativeQuery("insert into items (name, description, available, owner_id) values (?,?,?,?)")
                .setParameter(1, "itemName1")
                .setParameter(2, "itemDescription1")
                .setParameter(3, true)
                .setParameter(4, 1)
                .executeUpdate();
        User userOwner = new User(1, "userOwnerName", "userOwnerEmail@mail.ru");
        User userBooker = new User(2, "userBookerName", "userBookerEmail@mail.ru");
        Item item = new Item(1, "itemName1", "itemDescription1", true, userOwner, null);
        Booking booking = new Booking(1L,
                LocalDateTime.of(2023, Month.APRIL, 2, 12, 12, 12),
                LocalDateTime.of(2024, Month.APRIL, 2, 12, 12, 12),
                item,
                userBooker,
                Status.WAITING);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);
        bookingService.createBooking(2, bookingDto);
        TypedQuery<Booking> query = em.createQuery("select u from Booking u where u.booker = :booker", Booking.class);
        Booking bookingReturned = query.setParameter("booker", userBooker).getSingleResult();
        assertThat(bookingReturned, equalTo(booking));
    }
}
