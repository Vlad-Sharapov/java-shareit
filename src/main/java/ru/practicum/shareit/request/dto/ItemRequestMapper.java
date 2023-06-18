package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {


    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User user) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(itemRequestDto.getId());
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequestor(user);
        return itemRequest;
    }


    public static ItemRequestDto toDto(ItemRequest itemRequest) {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(itemRequest.getId());
        itemRequestDto.setDescription(itemRequest.getDescription());
        itemRequestDto.setCreated(itemRequest.getCreated());
        itemRequestDto.setRequestorId(itemRequest.getRequestor().getId());
        return itemRequestDto;
    }

    public static ItemRequestDto toDtoWithItems(ItemRequest itemRequest, Collection<Item> itemsOnRequests) {
        ItemRequestDto itemRequestDto = toDto(itemRequest);
        List<Item> itemsOnRequest = itemsOnRequests.stream()
                .filter(item -> item.getRequest().getId().equals(itemRequest.getId()))
                .collect(Collectors.toList());
        itemRequestDto.setItems(ItemMapper.toItemDto(itemsOnRequest));
        return itemRequestDto;
    }

    public static List<ItemRequestDto> toDtoWithItems(Collection<ItemRequest> itemRequests, Collection<Item> itemsOnRequests) {
        return itemRequests.stream()
                .map(itemRequest -> toDtoWithItems(itemRequest, itemsOnRequests))
                .collect(Collectors.toList());
    }

}
