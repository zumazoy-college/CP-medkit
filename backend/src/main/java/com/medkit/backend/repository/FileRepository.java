package com.medkit.backend.repository;

import com.medkit.backend.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Integer> {

    List<FileEntity> findByAppointment_IdAppointmentOrderByCreatedAtDesc(Integer appointmentId);
}
