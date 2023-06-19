package ru.practicum.shareit.request.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureTestDatabase
@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    ItemRequestService itemRequestService;

    User user1 = User.builder().id(1L).name("user1").email("newuser1@mail.ru").build();

    User user2 = User.builder().id(2L).name("user2").email("newuser2@mail.ru").build();

    List<ItemRequest> itemRequestsUser1 = List.of(
            ItemRequest.builder()
                    .id(1L)
                    .description("Учебник китайского")
                    .requestor(user1)
                    .created(LocalDateTime.now())
                    .build(),

            ItemRequest.builder()
                    .id(2L)
                    .description("Учебник английского")
                    .requestor(user1)
                    .created(LocalDateTime.now().minusDays(1))
                    .build());

    Item item = Item.builder().id(1L)
            .name("Коркуниан А.Ф. Практический курс китайского языка. 12-е изд. в двух томах")
            .description("Учебник по Китайскому языку")
            .available(true)
            .owner(user2)
            .request(itemRequestsUser1.get(0))
            .build();

    ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .description("Учебник китайского")
            .requestorId(user1.getId())
            .created(LocalDateTime.now())
            .build();

    @BeforeEach
    public void createItemRequestService() {
        itemRequestService =
                new ItemRequestServiceImpl(userRepository, itemRequestRepository, itemRepository);
    }

    @Test
    void createRequest() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.ofNullable(user1));
        when(itemRequestRepository.save(ItemRequestMapper.toItemRequest(itemRequestDto, user1)))
                .thenReturn(itemRequestsUser1.get(0));

        ItemRequestDto request = itemRequestService.createRequest(user1.getId(), itemRequestDto);

        assertThat(request, allOf(
                hasProperty("id", equalTo(itemRequestsUser1.get(0).getId())),
                hasProperty("description", equalTo(itemRequestDto.getDescription())),
                hasProperty("requestorId", equalTo(user1.getId()))
        ));
        assertThat(request.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    void shouldThrowUserNotFoundWhenUseCreateRequestWithUnknownUser() {
        ItemRequestService itemRequestService =
                new ItemRequestServiceImpl(userRepository, itemRequestRepository, itemRepository);
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.createRequest(user1.getId(), itemRequestDto));

    }

    @Test
    void getUserRequests() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.ofNullable(user1));
        when(itemRequestRepository.findByRequestorId(Mockito.anyLong(), Mockito.any(Sort.class)))
                .thenReturn(itemRequestsUser1);
        when(itemRepository.findByRequestIdIn(Mockito.anyCollection()))
                .thenReturn(List.of(item));

        List<ItemRequestDto> userRequests = itemRequestService.getUserRequests(user1.getId());
        for (ItemRequest userRequest : itemRequestsUser1) {
            assertThat(userRequests, hasItem(allOf(
                    hasProperty("id", equalTo(userRequest.getId())),
                    hasProperty("description", equalTo(userRequest.getDescription())),
                    hasProperty("requestorId", equalTo(userRequest.getRequestor().getId())),
                    hasProperty("created", equalTo(userRequest.getCreated()))
            )));
        }

        assertThat(userRequests.get(0).getItems(), hasItem(allOf(
                hasProperty("id", equalTo(item.getId())),
                hasProperty("name", equalTo(item.getName())),
                hasProperty("ownerId", equalTo(item.getOwner().getId())))
        ));
    }

    @Test
    void shouldThrowUserNotFoundWhenUseUserRequestsWithUnknownUser() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.getUserRequests(user1.getId()));
    }


    @Test
    void shouldThrowUserNotFoundWhenUseUserRequestsWithNoneRequest() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.ofNullable(user1));
        when(itemRequestRepository.findByRequestorId(Mockito.anyLong(), Mockito.any(Sort.class)))
                .thenReturn(new ArrayList<>());
        when(itemRepository.findByRequestIdIn(Mockito.anyCollection()))
                .thenReturn(List.of(item));

        List<ItemRequestDto> userRequests = itemRequestService.getUserRequests(user1.getId());

        assertThat(userRequests, equalTo(new ArrayList<>()));
    }

    @Test
    void shouldThrowUserNotFoundWhenUseUserRequestsWithNoneItems() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.ofNullable(user1));
        when(itemRequestRepository.findByRequestorId(Mockito.anyLong(), Mockito.any(Sort.class)))
                .thenReturn(itemRequestsUser1);
        when(itemRepository.findByRequestIdIn(Mockito.anyCollection()))
                .thenReturn(new ArrayList<>());

        List<ItemRequestDto> userRequests = itemRequestService.getUserRequests(user1.getId());

        for (ItemRequest userRequest : itemRequestsUser1) {
            assertThat(userRequests, hasItem(allOf(
                    hasProperty("id", equalTo(userRequest.getId())),
                    hasProperty("description", equalTo(userRequest.getDescription())),
                    hasProperty("requestorId", equalTo(userRequest.getRequestor().getId())),
                    hasProperty("created", equalTo(userRequest.getCreated())),
                    hasProperty("items", emptyCollectionOf(Item.class)
                    )
            )));
        }
    }

    @Test
    void getAllRequests() {
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("created").descending());
        Page<ItemRequest> pages = new PageImpl<>(itemRequestsUser1, pageRequest, itemRequestsUser1.size());
        when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.ofNullable(user2));
        when(itemRequestRepository.findAll(pageRequest))
                .thenReturn(pages);
        when(itemRepository.findByRequestIdIn(Mockito.anyCollection()))
                .thenReturn(List.of(item));

        List<ItemRequestDto> allRequests = itemRequestService.getAllRequests(user2.getId(), 0, 20);

        for (ItemRequest userRequest : itemRequestsUser1) {
            assertThat(allRequests, hasItem(allOf(
                    hasProperty("id", equalTo(userRequest.getId())),
                    hasProperty("description", equalTo(userRequest.getDescription())),
                    hasProperty("requestorId", equalTo(userRequest.getRequestor().getId())),
                    hasProperty("created", equalTo(userRequest.getCreated()))
            )));
        }
        assertThat(allRequests.get(0).getItems(), hasItem(allOf(
                hasProperty("id", equalTo(item.getId())),
                hasProperty("name", equalTo(item.getName())),
                hasProperty("ownerId", equalTo(item.getOwner().getId())))
        ));
    }

    @Test
    void shouldEmptyListWhenUserHasOnlyHisRequests() {
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("created").descending());
        Page<ItemRequest> pages = new PageImpl<>(itemRequestsUser1, pageRequest, itemRequestsUser1.size());
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.ofNullable(user1));
        when(itemRequestRepository.findAll(pageRequest))
                .thenReturn(pages);
        when(itemRepository.findByRequestIdIn(Mockito.anyCollection()))
                .thenReturn(List.of(item));

        List<ItemRequestDto> allRequests = itemRequestService.getAllRequests(user1.getId(), 0, 20);

        assertThat(allRequests, equalTo(new ArrayList<>()));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUseGetAllRequestsWithUnknownUser() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.getAllRequests(user1.getId(), 0, 20));
    }


    @Test
    void getRequest() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.ofNullable(user1));
        when(itemRequestRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(itemRequestsUser1.get(0)));
        when(itemRepository.findByRequestId(Mockito.anyLong()))
                .thenReturn(List.of(item));

        ItemRequestDto request = itemRequestService.getRequest(user1.getId(), itemRequestsUser1.get(1).getId());

        assertThat(request, allOf(
                hasProperty("id", equalTo(itemRequestsUser1.get(0).getId())),
                hasProperty("description", equalTo(itemRequestsUser1.get(0).getDescription())),
                hasProperty("requestorId", equalTo(itemRequestsUser1.get(0).getRequestor().getId())),
                hasProperty("created", equalTo(itemRequestsUser1.get(0).getCreated()))
        ));

        assertThat(request.getItems(), hasItem(allOf(
                hasProperty("id", equalTo(item.getId())),
                hasProperty("name", equalTo(item.getName())),
                hasProperty("ownerId", equalTo(item.getOwner().getId())))
        ));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUseGetRequestWithUnknownUser() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.getRequest(user1.getId(), 1L));

    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUseGetRequestWithUnknownRequest() {
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.ofNullable(user1));
        when(itemRequestRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.getRequest(user1.getId(), 1L));

    }
}