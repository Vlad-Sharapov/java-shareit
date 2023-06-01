package ru.practicum.shareit.booking.model;

import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.Instant;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_booking",nullable = false)
    private Instant start;

    @Column(name = "end_booking",nullable = false)
    private Instant end;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item",nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "booker",nullable = false)
    private User booker;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
}
