package com.medkit.backend.repository;

import com.medkit.backend.entity.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlotStatusRepository extends JpaRepository<SlotStatus, Integer> {

    Optional<SlotStatus> findByTitle(String title);
}
