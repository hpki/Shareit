package ru.practicum.shareit.requests;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRequestStorage extends JpaRepository<ItemRequest, Long> {
    Page<ItemRequest> findAllByRequestorIdIsNot(long requestorId, Pageable pageable);
}
