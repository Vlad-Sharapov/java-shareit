package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(String description, String name, Pageable pageable);

    List<Item> findByOwnerId(Long ownerId);

    List<Item> findByRequestIdIn(Collection<Long> requestsIds);

    List<Item> findByRequestId(Long requestId);

}
