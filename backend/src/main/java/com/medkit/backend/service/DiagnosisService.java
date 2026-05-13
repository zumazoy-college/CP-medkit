package com.medkit.backend.service;

import com.medkit.backend.dto.request.AddDiagnosisRequest;
import com.medkit.backend.dto.response.AppointmentDiagnosisResponse;
import com.medkit.backend.dto.response.DiagnosisResponse;
import com.medkit.backend.entity.*;
import com.medkit.backend.exception.ResourceNotFoundException;
import com.medkit.backend.exception.UnauthorizedException;
import com.medkit.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final AppointmentDiagnosisRepository appointmentDiagnosisRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Transactional
    public AppointmentDiagnosisResponse addDiagnosisToAppointment(String userEmail, AddDiagnosisRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        if (!appointment.getSlot().getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new UnauthorizedException("Вы не можете добавить диагноз к этому приему");
        }

        Diagnosis diagnosis = diagnosisRepository.findById(request.getDiagnosisId())
                .orElseThrow(() -> new ResourceNotFoundException("Диагноз не найден"));

        // Check if diagnosis already exists for this appointment
        boolean exists = appointmentDiagnosisRepository.findByAppointment_IdAppointment(appointment.getIdAppointment())
                .stream()
                .anyMatch(ad -> ad.getDiagnosis().getIdDiagnosis().equals(diagnosis.getIdDiagnosis()));

        if (exists) {
            throw new IllegalStateException("Этот диагноз уже добавлен к приему");
        }

        // If this diagnosis should be primary, set all other diagnoses to non-primary
        if (request.getIsPrimary() != null && request.getIsPrimary()) {
            List<AppointmentDiagnosis> allDiagnoses = appointmentDiagnosisRepository
                    .findByAppointment_IdAppointment(appointment.getIdAppointment());

            for (AppointmentDiagnosis ad : allDiagnoses) {
                ad.setIsPrimary(false);
                appointmentDiagnosisRepository.save(ad);
            }
        }

        AppointmentDiagnosis appointmentDiagnosis = new AppointmentDiagnosis();
        appointmentDiagnosis.setAppointment(appointment);
        appointmentDiagnosis.setDiagnosis(diagnosis);
        appointmentDiagnosis.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);

        AppointmentDiagnosis saved = appointmentDiagnosisRepository.save(appointmentDiagnosis);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDiagnosisResponse> getAppointmentDiagnoses(Integer appointmentId) {
        return appointmentDiagnosisRepository.findByAppointment_IdAppointment(appointmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDiagnosisResponse> getPatientDiagnoses(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        // Get all completed appointments for the patient
        List<Appointment> completedAppointments = appointmentRepository
                .findByPatient_IdPatientAndSlot_Status_TitleOrderByCreatedAtDesc(
                        patient.getIdPatient(), "completed");

        // Get all diagnoses from these appointments
        return completedAppointments.stream()
                .flatMap(appointment -> appointmentDiagnosisRepository
                        .findByAppointment_IdAppointment(appointment.getIdAppointment())
                        .stream())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<DiagnosisResponse> searchDiagnoses(String query, Pageable pageable) {
        return diagnosisRepository.findByIcdNameContainingIgnoreCaseOrIcdCodeContainingIgnoreCase(query, query, pageable)
                .map(this::mapDiagnosisToResponse);
    }

    @Transactional(readOnly = true)
    public DiagnosisResponse getDiagnosisByCode(String icdCode) {
        Diagnosis diagnosis = diagnosisRepository.findByIcdCode(icdCode)
                .orElseThrow(() -> new ResourceNotFoundException("Диагноз не найден с кодом: " + icdCode));
        return mapDiagnosisToResponse(diagnosis);
    }

    @Transactional
    public void removeDiagnosisFromAppointment(String userEmail, Integer appointmentDiagnosisId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        AppointmentDiagnosis appointmentDiagnosis = appointmentDiagnosisRepository.findById(appointmentDiagnosisId)
                .orElseThrow(() -> new ResourceNotFoundException("Диагноз приема не найден"));

        if (!appointmentDiagnosis.getAppointment().getSlot().getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new UnauthorizedException("Вы не можете удалить этот диагноз");
        }

        // Check if appointment is cancelled
        if (appointmentDiagnosis.getAppointment().getSlot().getCancellationReason() != null) {
            throw new IllegalStateException("Невозможно удалить диагноз из отмененного приема");
        }

        appointmentDiagnosisRepository.delete(appointmentDiagnosis);
    }

    @Transactional
    public AppointmentDiagnosisResponse setPrimaryDiagnosis(String userEmail, Integer appointmentDiagnosisId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        AppointmentDiagnosis appointmentDiagnosis = appointmentDiagnosisRepository.findById(appointmentDiagnosisId)
                .orElseThrow(() -> new ResourceNotFoundException("Диагноз приема не найден"));

        if (!appointmentDiagnosis.getAppointment().getSlot().getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new UnauthorizedException("Вы не можете изменить этот диагноз");
        }

        // Check if appointment is cancelled
        if (appointmentDiagnosis.getAppointment().getSlot().getCancellationReason() != null) {
            throw new IllegalStateException("Невозможно изменить диагноз в отмененном приеме");
        }

        // Set all other diagnoses for this appointment to non-primary
        List<AppointmentDiagnosis> allDiagnoses = appointmentDiagnosisRepository
                .findByAppointment_IdAppointment(appointmentDiagnosis.getAppointment().getIdAppointment());

        for (AppointmentDiagnosis ad : allDiagnoses) {
            ad.setIsPrimary(false);
            appointmentDiagnosisRepository.save(ad);
        }

        // Set this diagnosis as primary
        appointmentDiagnosis.setIsPrimary(true);
        AppointmentDiagnosis saved = appointmentDiagnosisRepository.save(appointmentDiagnosis);

        return mapToResponse(saved);
    }

    private AppointmentDiagnosisResponse mapToResponse(AppointmentDiagnosis appointmentDiagnosis) {
        AppointmentDiagnosisResponse response = new AppointmentDiagnosisResponse();
        response.setId(appointmentDiagnosis.getIdAppointmentDiagnosis());
        response.setAppointmentId(appointmentDiagnosis.getAppointment().getIdAppointment());
        response.setDiagnosisId(appointmentDiagnosis.getDiagnosis().getIdDiagnosis());
        response.setIcdCode(appointmentDiagnosis.getDiagnosis().getIcdCode());
        response.setDiagnosisName(appointmentDiagnosis.getDiagnosis().getIcdName());
        response.setIsPrimary(appointmentDiagnosis.getIsPrimary());
        response.setAppointmentDate(appointmentDiagnosis.getAppointment().getSlot().getSlotDate());
        return response;
    }

    private DiagnosisResponse mapDiagnosisToResponse(Diagnosis diagnosis) {
        DiagnosisResponse response = new DiagnosisResponse();
        response.setIdDiagnosis(diagnosis.getIdDiagnosis());
        response.setIcdCode(diagnosis.getIcdCode());
        response.setName(diagnosis.getIcdName());
        return response;
    }
}
