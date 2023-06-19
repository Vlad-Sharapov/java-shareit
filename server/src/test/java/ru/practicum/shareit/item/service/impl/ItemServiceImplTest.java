package ru.practicum.shareit.item.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoByOwner;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureTestDatabase
@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest extends EntitiesForItemTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    private ItemService itemService;

    private final Item item1 = items.get(0);

    private final Item item2 = items.get(1);

    private final Booking booking1 = bookings.get(0);

    private final Booking booking2 = bookings.get(1);

    @BeforeEach
    public void beforeEach() {
        itemService = new ItemServiceImpl(itemRepository, userRepository,
                bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    void saveItem() {
        Item newItem = item1.toBuilder().id(1L).build();
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(newItem);
        when(itemRequestRepository.findById(item1.getRequest().getId()))
                .thenReturn(Optional.of(itemRequestUser1));

        ItemDto responseItemDto = itemService.saveItem(user1.getId(), itemDto);

        assertThat(responseItemDto, allOf(
                hasProperty("id", equalTo(newItem.getId())),
                hasProperty("name", equalTo(newItem.getName())),
                hasProperty("description", equalTo(newItem.getDescription())),
                hasProperty("ownerId", equalTo(newItem.getOwner().getId())),
                hasProperty("available", equalTo(newItem.getAvailable())),
                hasProperty("requestId", equalTo(itemRequestUser1.getId()))
        ));
    }

    @Test
    void shouldSaveItemWithoutRequestWhenUseSaveItemWithoutRequestIdParameter() {
        Item newItem = item1.toBuilder().build();
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(newItem);
        when(itemRequestRepository.findById(item1.getRequest().getId()))
                .thenReturn(Optional.ofNullable(itemRequestUser1));

        ItemDto responseItemDto = itemService.saveItem(user1.getId(), itemDto);

        assertThat(responseItemDto, allOf(
                hasProperty("id", equalTo(newItem.getId())),
                hasProperty("name", equalTo(newItem.getName())),
                hasProperty("description", equalTo(newItem.getDescription())),
                hasProperty("ownerId", equalTo(newItem.getOwner().getId())),
                hasProperty("available", equalTo(newItem.getAvailable())),
                hasProperty("requestId", equalTo(itemRequestUser1.getId()))
        ));
    }

    @Test
    void updateItem() {
        Item oldItem = item1.toBuilder().build();
        Item updatedItem = item1.toBuilder()
                .id(1L)
                .description("update desc")
                .name("updateName")
                .available(false)
                .build();
        when(itemRepository.findById(oldItem.getId()))
                .thenReturn(Optional.of(oldItem));
        when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(updatedItem);

        itemService.updateItem(user2.getId(),
                oldItem.getId(),
                ItemDto.builder()
                        .name(updatedItem.getName())
                        .description(updatedItem.getDescription())
                        .available(updatedItem.getAvailable())
                        .build());

        verify(itemRepository, Mockito.times(1))
                .save(updatedItem);
    }

    @Test
    void shouldUpdateOnlyDescriptionWhenUseUpdateItemWithDescriptionFieldInBody() {
        Item oldItem = item1.toBuilder().build();
        Item updatedItem = item1.toBuilder()
                .id(1L)
                .description("update desc")
                .build();
        when(itemRepository.findById(oldItem.getId()))
                .thenReturn(Optional.of(oldItem));
        when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(updatedItem);

        itemService.updateItem(user2.getId(),
                oldItem.getId(),
                ItemDto.builder()
                        .name(updatedItem.getName())
                        .description(updatedItem.getDescription())
                        .available(updatedItem.getAvailable())
                        .build());

        verify(itemRepository, Mockito.times(1))
                .save(updatedItem);
    }

    @Test
    void shouldUpdateOnlyNameWhenUseUpdateItemWithNameFieldInBody() {
        Item oldItem = item1.toBuilder().build();
        Item updatedItem = item1.toBuilder()
                .id(1L)
                .name("updateName")
                .build();
        when(itemRepository.findById(oldItem.getId()))
                .thenReturn(Optional.of(oldItem));
        when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(updatedItem);

        itemService.updateItem(user2.getId(),
                oldItem.getId(),
                ItemDto.builder()
                        .name(updatedItem.getName())
                        .description(updatedItem.getDescription())
                        .available(updatedItem.getAvailable())
                        .build());

        verify(itemRepository, Mockito.times(1))
                .save(updatedItem);
    }

    @Test
    void shouldUpdateOnlyAvailableWhenUseUpdateItemWithAvailableFieldInBody() {
        Item oldItem = item1.toBuilder().build();
        Item updatedItem = item1.toBuilder()
                .id(1L)
                .available(false)
                .build();
        when(itemRepository.findById(oldItem.getId()))
                .thenReturn(Optional.of(oldItem));
        when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(updatedItem);

        itemService.updateItem(user2.getId(),
                oldItem.getId(),
                ItemDto.builder()
                        .name(updatedItem.getName())
                        .description(updatedItem.getDescription())
                        .available(updatedItem.getAvailable())
                        .build());

        verify(itemRepository, Mockito.times(1))
                .save(updatedItem);
    }

    @Test
    void shouldUpdateAvailableAndDescriptionWhenUseUpdateItemWithAvailableAndDescriptionFieldsInBody() {
        Item oldItem = item1.toBuilder().build();
        Item updatedItem = item1.toBuilder()
                .id(1L)
                .description("new description")
                .available(false)
                .build();
        when(itemRepository.findById(oldItem.getId()))
                .thenReturn(Optional.of(oldItem));
        when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(updatedItem);

        itemService.updateItem(user2.getId(),
                oldItem.getId(),
                ItemDto.builder()
                        .name(updatedItem.getName())
                        .description(updatedItem.getDescription())
                        .available(updatedItem.getAvailable())
                        .build());

        verify(itemRepository, Mockito.times(1))
                .save(updatedItem);
    }

    @Test
    void shouldThrowExceptionWhenUseUpdateItemNotOwner() {
        Item oldItem = item1.toBuilder().build();
        Item updatedItem = item1.toBuilder()
                .id(1L)
                .description("update desc")
                .name("updateName")
                .available(false)
                .build();
        when(itemRepository.findById(oldItem.getId()))
                .thenReturn(Optional.of(oldItem));

        assertThrows(EntityNotFoundException.class,
                () -> itemService.updateItem(user1.getId(),
                        oldItem.getId(),
                        ItemDto.builder()
                                .name(updatedItem.getName())
                                .description(updatedItem.getDescription())
                                .available(updatedItem.getAvailable())
                                .build()));

    }


    @Test
    void getAllUserItems() {
        when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));
        when(itemRepository.findByOwnerId(user2.getId()))
                .thenReturn(List.of(item1));
        when(bookingRepository.findByItemOwnerId(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(List.of(booking1, booking2));
        when(commentRepository.findByItem_IdIn(List.of(item1.getId())))
                .thenReturn(List.of(comment));

        List<ItemDtoByOwner> allUserItems = itemService.getUserItems(user2.getId(), 0, 20);

        assertThat(allUserItems, hasItem(allOf(
                hasProperty("id", equalTo(item1.getId())),
                hasProperty("name", equalTo(item1.getName())),
                hasProperty("description", equalTo(item1.getDescription())),
                hasProperty("available", equalTo(item1.getAvailable())),
                hasProperty("requestId", equalTo(item1.getRequest().getId())),
                hasProperty("lastBooking", allOf(
                        hasProperty("id", equalTo(booking2.getId())),
                        hasProperty("start", equalTo(booking2.getStart())),
                        hasProperty("end", equalTo(booking2.getEnd())),
                        hasProperty("itemId", equalTo(booking2.getItem().getId())),
                        hasProperty("bookerId", equalTo(booking2.getBooker().getId())),
                        hasProperty("status", equalTo(booking2.getStatus())
                        ))),
                hasProperty("nextBooking", allOf(
                        hasProperty("id", equalTo(booking1.getId())),
                        hasProperty("start", equalTo(booking1.getStart())),
                        hasProperty("end", equalTo(booking1.getEnd())),
                        hasProperty("itemId", equalTo(booking1.getItem().getId())),
                        hasProperty("bookerId", equalTo(booking1.getBooker().getId())),
                        hasProperty("status", equalTo(booking1.getStatus())
                        ))),
                hasProperty("comments", hasItem(allOf(
                        hasProperty("id", equalTo(comment.getId())),
                        hasProperty("text", equalTo(comment.getText())),
                        hasProperty("itemId", equalTo(comment.getItem().getId())),
                        hasProperty("authorName", equalTo(comment.getAuthor().getName())),
                        hasProperty("created", equalTo(comment.getCreated()))
                )))
        )));
    }

    @Test
    void shouldItemWithOnlyNextBookingWhenUseGetAllUserItemsWithTwoNextBookings() {
        Booking nextBooking1 = booking1.toBuilder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        Booking nextBooking2 = booking2.toBuilder()
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));
        when(itemRepository.findByOwnerId(user2.getId()))
                .thenReturn(List.of(item1));
        when(bookingRepository.findByItemOwnerId(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(List.of(nextBooking1, nextBooking2));
        when(commentRepository.findByItem_IdIn(List.of(item1.getId())))
                .thenReturn(List.of(comment));

        List<ItemDtoByOwner> allUserItems = itemService.getUserItems(user2.getId(), 0, 20);

        assertThat(allUserItems, hasItem(allOf(
                hasProperty("id", equalTo(item1.getId())),
                hasProperty("name", equalTo(item1.getName())),
                hasProperty("description", equalTo(item1.getDescription())),
                hasProperty("available", equalTo(item1.getAvailable())),
                hasProperty("requestId", equalTo(item1.getRequest().getId())),
                hasProperty("lastBooking", nullValue()),
                hasProperty("nextBooking", allOf(
                        hasProperty("id", equalTo(nextBooking1.getId())),
                        hasProperty("start", equalTo(nextBooking1.getStart())),
                        hasProperty("end", equalTo(nextBooking1.getEnd())),
                        hasProperty("itemId", equalTo(nextBooking1.getItem().getId())),
                        hasProperty("bookerId", equalTo(nextBooking1.getBooker().getId())),
                        hasProperty("status", equalTo(nextBooking1.getStatus())
                        ))),
                hasProperty("comments", hasItem(allOf(
                        hasProperty("id", equalTo(comment.getId())),
                        hasProperty("text", equalTo(comment.getText())),
                        hasProperty("itemId", equalTo(comment.getItem().getId())),
                        hasProperty("authorName", equalTo(comment.getAuthor().getName())),
                        hasProperty("created", equalTo(comment.getCreated()))
                )))
        )));
    }

    @Test
    void shouldItemWithOnlyLastBookingWhenUseGetAllUserItemsWithTwoLastBookings() {
        Booking nextBooking1 = booking1.toBuilder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build();
        Booking nextBooking2 = booking2.toBuilder()
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .build();
        when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));
        when(itemRepository.findByOwnerId(user2.getId()))
                .thenReturn(List.of(item1));
        when(bookingRepository.findByItemOwnerId(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(List.of(nextBooking1, nextBooking2));
        when(commentRepository.findByItem_IdIn(List.of(item1.getId())))
                .thenReturn(List.of(comment));

        List<ItemDtoByOwner> allUserItems = itemService.getUserItems(user2.getId(), 0, 20);

        assertThat(allUserItems, hasItem(allOf(
                hasProperty("id", equalTo(item1.getId())),
                hasProperty("name", equalTo(item1.getName())),
                hasProperty("description", equalTo(item1.getDescription())),
                hasProperty("available", equalTo(item1.getAvailable())),
                hasProperty("requestId", equalTo(item1.getRequest().getId())),
                hasProperty("nextBooking", nullValue()),
                hasProperty("lastBooking", allOf(
                        hasProperty("id", equalTo(nextBooking1.getId())),
                        hasProperty("start", equalTo(nextBooking1.getStart())),
                        hasProperty("end", equalTo(nextBooking1.getEnd())),
                        hasProperty("itemId", equalTo(nextBooking1.getItem().getId())),
                        hasProperty("bookerId", equalTo(nextBooking1.getBooker().getId())),
                        hasProperty("status", equalTo(nextBooking1.getStatus())
                        ))),
                hasProperty("comments", hasItem(allOf(
                        hasProperty("id", equalTo(comment.getId())),
                        hasProperty("text", equalTo(comment.getText())),
                        hasProperty("itemId", equalTo(comment.getItem().getId())),
                        hasProperty("authorName", equalTo(comment.getAuthor().getName())),
                        hasProperty("created", equalTo(comment.getCreated()))
                )))
        )));
    }

    @Test
    void shouldEmptyListWhenUserWithoutItemsUseGetAllUserItems() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(itemRepository.findByOwnerId(user1.getId()))
                .thenReturn(List.of());
        when(bookingRepository.findByItemOwnerId(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(List.of());
        when(commentRepository.findByItem_IdIn(List.of()))
                .thenReturn(List.of());

        List<ItemDtoByOwner> allUserItems = itemService.getUserItems(user1.getId(), 0, 20);

        assertThat(allUserItems, emptyCollectionOf(ItemDtoByOwner.class));
    }

    @Test
    void getItem() {
        when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));
        when(itemRepository.findById(item1.getId()))
                .thenReturn(Optional.of(item1));
        when(bookingRepository.findByItem_IdAndItemOwnerId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(List.of(booking1, booking2));
        when(commentRepository.findByItem_Id(item1.getId()))
                .thenReturn(List.of(comment));

        ItemDtoByOwner foundItem = itemService.getItem(user2.getId(), item1.getId());

        assertThat(foundItem, allOf(
                hasProperty("id", equalTo(item1.getId())),
                hasProperty("name", equalTo(item1.getName())),
                hasProperty("description", equalTo(item1.getDescription())),
                hasProperty("available", equalTo(item1.getAvailable())),
                hasProperty("requestId", equalTo(item1.getRequest().getId())),
                hasProperty("lastBooking", allOf(
                        hasProperty("id", equalTo(booking2.getId())),
                        hasProperty("start", equalTo(booking2.getStart())),
                        hasProperty("end", equalTo(booking2.getEnd())),
                        hasProperty("itemId", equalTo(booking2.getItem().getId())),
                        hasProperty("bookerId", equalTo(booking2.getBooker().getId())),
                        hasProperty("status", equalTo(booking2.getStatus())
                        ))),
                hasProperty("nextBooking", allOf(
                        hasProperty("id", equalTo(booking1.getId())),
                        hasProperty("start", equalTo(booking1.getStart())),
                        hasProperty("end", equalTo(booking1.getEnd())),
                        hasProperty("itemId", equalTo(booking1.getItem().getId())),
                        hasProperty("bookerId", equalTo(booking1.getBooker().getId())),
                        hasProperty("status", equalTo(booking1.getStatus())
                        ))),
                hasProperty("comments", hasItem(allOf(
                        hasProperty("id", equalTo(comment.getId())),
                        hasProperty("text", equalTo(comment.getText())),
                        hasProperty("itemId", equalTo(comment.getItem().getId())),
                        hasProperty("authorName", equalTo(comment.getAuthor().getName())),
                        hasProperty("created", equalTo(comment.getCreated()))
                )))
        ));
    }

    @Test
    void shouldUserGetItemWithoutBookings() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(itemRepository.findById(item1.getId()))
                .thenReturn(Optional.of(item1));
        when(bookingRepository.findByItem_IdAndItemOwnerId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(List.of());
        when(commentRepository.findByItem_Id(item1.getId()))
                .thenReturn(List.of(comment));

        ItemDtoByOwner foundItem = itemService.getItem(user1.getId(), item1.getId());

        assertThat(foundItem, allOf(
                hasProperty("id", equalTo(item1.getId())),
                hasProperty("name", equalTo(item1.getName())),
                hasProperty("description", equalTo(item1.getDescription())),
                hasProperty("available", equalTo(item1.getAvailable())),
                hasProperty("requestId", equalTo(item1.getRequest().getId())),
                hasProperty("lastBooking", nullValue()),
                hasProperty("nextBooking", nullValue()),
                hasProperty("comments", hasItem(allOf(
                        hasProperty("id", equalTo(comment.getId())),
                        hasProperty("text", equalTo(comment.getText())),
                        hasProperty("itemId", equalTo(comment.getItem().getId())),
                        hasProperty("authorName", equalTo(comment.getAuthor().getName())),
                        hasProperty("created", equalTo(comment.getCreated()))
                )))
        ));
    }

    @Test
    void search() {
        String text = "Учебник";
        when(itemRepository.findAllByDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(Pageable.class)
        )).thenReturn(List.of(item1, item2));
        List<ItemDto> search = itemService.search(text, 0, 20);

        assertThat(search, hasSize(1));
        assertThat(search.get(0), allOf(
                hasProperty("id", equalTo(item1.getId())),
                hasProperty("name", equalTo(item1.getName())),
                hasProperty("description", equalTo(item1.getDescription())),
                hasProperty("available", equalTo(item1.getAvailable()))
        ));
    }

    @Test
    void shouldEmptyListWhenTextIsBlank() {
        String text = "";
        List<ItemDto> search = itemService.search(text, 0, 20);

        assertThat(search, hasSize(0));
    }

    @Test
    void addComment() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(itemRepository.findById(item1.getId()))
                .thenReturn(Optional.of(item1));
        when(bookingRepository.findTopByStatusNotLikeAndBookerIdAndItemId(Mockito.any(BookingStatus.class),
                Mockito.anyLong(),
                Mockito.anyLong(),
                Mockito.any(Sort.class)))
                .thenReturn(Optional.of(booking2));
        when(commentRepository.save(Mockito.any(Comment.class)))
                .thenReturn(comment);

        CommentDto commentItem = itemService.addComment(user1.getId(), item1.getId(), CommentDto.builder()
                .text(comment.getText())
                .build());

        assertThat(commentItem, allOf(
                hasProperty("id", equalTo(comment.getId())),
                hasProperty("text", equalTo(comment.getText())),
                hasProperty("itemId", equalTo(comment.getItem().getId())),
                hasProperty("authorName", equalTo(comment.getAuthor().getName())),
                hasProperty("created", equalTo(comment.getCreated()))
        ));
    }

    @Test
    void shouldNotCreatedCommentWhenUserNotUsedItem() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(itemRepository.findById(item1.getId()))
                .thenReturn(Optional.of(item1));
        when(bookingRepository.findTopByStatusNotLikeAndBookerIdAndItemId(Mockito.any(BookingStatus.class),
                Mockito.anyLong(),
                Mockito.anyLong(),
                Mockito.any(Sort.class)))
                .thenReturn(Optional.ofNullable(booking1));

        assertThrows(BadRequestException.class,
                () -> itemService.addComment(user1.getId(), item1.getId(), CommentDto.builder()
                        .text(comment.getText())
                        .build()));
    }

    @Test
    void shouldNotCreatedCommentWhenItemRejectedOrNoneBookingOrUser() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(itemRepository.findById(item1.getId()))
                .thenReturn(Optional.of(item1));
        when(bookingRepository.findTopByStatusNotLikeAndBookerIdAndItemId(Mockito.any(BookingStatus.class),
                Mockito.anyLong(),
                Mockito.anyLong(),
                Mockito.any(Sort.class)))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> itemService.addComment(user1.getId(), item1.getId(), CommentDto.builder()
                        .text(comment.getText())
                        .build()));
    }
}