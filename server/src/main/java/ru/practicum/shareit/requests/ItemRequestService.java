package ru.practicum.shareit.requests;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.requests.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getUserItemRequests(long userId);

    List<ItemRequestDto> getItemRequests(long userId, Pageable pageable);

    ItemRequestDto getItemRequestById(long userId, long requestId);
}
