package com.medkit.backend.service;

import com.medkit.backend.dto.request.CreateMedicationPrescriptionRequest;
import com.medkit.backend.dto.request.CreateProcedurePrescriptionRequest;
import com.medkit.backend.dto.request.CreateAnalysisPrescriptionRequest;
import com.medkit.backend.dto.response.MedicationPrescriptionResponse;
import com.medkit.backend.dto.response.ProcedurePrescriptionResponse;
import com.medkit.backend.dto.response.AnalysisPrescriptionResponse;
import com.medkit.backend.entity.*;
import com.medkit.backend.exception.ResourceNotFoundException;
import com.medkit.backend.exception.UnauthorizedException;
import com.medkit.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final MedicationPrescriptionRepository medicationPrescriptionRepository;
    private final ProcedurePrescriptionRepository procedurePrescriptionRepository;
    private final AnalysisPrescriptionRepository analysisPrescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicationRepository medicationRepository;
    private final ProcedureRepository procedureRepository;
    private final AnalysisRepository analysisRepository;
    private final PrescriptionStatusRepository prescriptionStatusRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public MedicationPrescriptionResponse createMedicationPrescription(String userEmail, CreateMedicationPrescriptionRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        if (!appointment.getSlot().getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new UnauthorizedException("Вы не можете создать рецепт для этого приема");
        }

        Medication medication = medicationRepository.findById(request.getMedicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Лекарство не найдено"));

        PrescriptionStatus activeStatus = prescriptionStatusRepository.findByTitle("active")
                .orElseThrow(() -> new ResourceNotFoundException("Статус 'active' не найден"));

        MedicationPrescription prescription = new MedicationPrescription();
        prescription.setAppointment(appointment);
        prescription.setMedication(medication);
        prescription.setDuration(request.getDuration().toString());
        prescription.setInstructions(request.getInstructions());
        prescription.setStatus(activeStatus);

        MedicationPrescription saved = medicationPrescriptionRepository.save(prescription);
        return mapMedicationToResponse(saved);
    }

    @Transactional
    public ProcedurePrescriptionResponse createProcedurePrescription(String userEmail, CreateProcedurePrescriptionRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        if (!appointment.getSlot().getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new UnauthorizedException("Вы не можете создать назначение для этого приема");
        }

        Procedure procedure = procedureRepository.findById(request.getProcedureId())
                .orElseThrow(() -> new ResourceNotFoundException("Процедура не найдена"));

        PrescriptionStatus activeStatus = prescriptionStatusRepository.findByTitle("active")
                .orElseThrow(() -> new ResourceNotFoundException("Статус 'active' не найден"));

        ProcedurePrescription prescription = new ProcedurePrescription();
        prescription.setAppointment(appointment);
        prescription.setProcedure(procedure);
        prescription.setQuantity(1);
        prescription.setInstructions(request.getInstructions());
        prescription.setStatus(activeStatus);

        ProcedurePrescription saved = procedurePrescriptionRepository.save(prescription);
        return mapProcedureToResponse(saved);
    }

    @Transactional
    public AnalysisPrescriptionResponse createAnalysisPrescription(String userEmail, CreateAnalysisPrescriptionRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        if (!appointment.getSlot().getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new UnauthorizedException("Вы не можете создать назначение для этого приема");
        }

        Analysis analysis = analysisRepository.findById(request.getAnalysisId())
                .orElseThrow(() -> new ResourceNotFoundException("Анализ не найден"));

        PrescriptionStatus activeStatus = prescriptionStatusRepository.findByTitle("active")
                .orElseThrow(() -> new ResourceNotFoundException("Статус 'active' не найден"));

        AnalysisPrescription prescription = new AnalysisPrescription();
        prescription.setAppointment(appointment);
        prescription.setAnalysis(analysis);
        prescription.setInstructions(request.getInstructions());
        prescription.setStatus(activeStatus);

        AnalysisPrescription saved = analysisPrescriptionRepository.save(prescription);
        return mapAnalysisToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MedicationPrescriptionResponse> getPatientMedicationPrescriptions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = user.getPatient();
        if (patient == null) {
            throw new ResourceNotFoundException("Пациент не найден");
        }

        return medicationPrescriptionRepository.findByAppointment_Patient_IdPatientOrderByCreatedAtDesc(patient.getIdPatient())
                .stream()
                .map(this::mapMedicationToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProcedurePrescriptionResponse> getPatientProcedurePrescriptions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = user.getPatient();
        if (patient == null) {
            throw new ResourceNotFoundException("Пациент не найден");
        }

        return procedurePrescriptionRepository.findByAppointment_Patient_IdPatientOrderByCreatedAtDesc(patient.getIdPatient())
                .stream()
                .map(this::mapProcedureToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnalysisPrescriptionResponse> getPatientAnalysisPrescriptions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = user.getPatient();
        if (patient == null) {
            throw new ResourceNotFoundException("Пациент не найден");
        }

        return analysisPrescriptionRepository.findByAppointment_Patient_IdPatientOrderByCreatedAtDesc(patient.getIdPatient())
                .stream()
                .map(this::mapAnalysisToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicationPrescriptionResponse> getAppointmentMedicationPrescriptions(String userEmail, Integer appointmentId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        // Проверяем доступ - любой врач или пациент этого приема
        boolean isAnyDoctor = doctorRepository.findByUser_IdUser(user.getIdUser()).isPresent();
        boolean isPatient = appointment.getPatient().getUser().getIdUser().equals(user.getIdUser());

        if (!isAnyDoctor && !isPatient) {
            throw new UnauthorizedException("У вас нет доступа к этому приему");
        }

        return medicationPrescriptionRepository.findByAppointment_IdAppointmentOrderByCreatedAtDesc(appointmentId)
                .stream()
                .map(this::mapMedicationToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProcedurePrescriptionResponse> getAppointmentProcedurePrescriptions(String userEmail, Integer appointmentId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        // Проверяем доступ - любой врач или пациент этого приема
        boolean isAnyDoctor = doctorRepository.findByUser_IdUser(user.getIdUser()).isPresent();
        boolean isPatient = appointment.getPatient().getUser().getIdUser().equals(user.getIdUser());

        if (!isAnyDoctor && !isPatient) {
            throw new UnauthorizedException("У вас нет доступа к этому приему");
        }

        return procedurePrescriptionRepository.findByAppointment_IdAppointmentOrderByCreatedAtDesc(appointmentId)
                .stream()
                .map(this::mapProcedureToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnalysisPrescriptionResponse> getAppointmentAnalysisPrescriptions(String userEmail, Integer appointmentId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        // Проверяем доступ - любой врач или пациент этого приема
        boolean isAnyDoctor = doctorRepository.findByUser_IdUser(user.getIdUser()).isPresent();
        boolean isPatient = appointment.getPatient().getUser().getIdUser().equals(user.getIdUser());

        if (!isAnyDoctor && !isPatient) {
            throw new UnauthorizedException("У вас нет доступа к этому приему");
        }

        return analysisPrescriptionRepository.findByAppointment_IdAppointmentOrderByCreatedAtDesc(appointmentId)
                .stream()
                .map(this::mapAnalysisToResponse)
                .collect(Collectors.toList());
    }

    private MedicationPrescriptionResponse mapMedicationToResponse(MedicationPrescription prescription) {
        MedicationPrescriptionResponse response = new MedicationPrescriptionResponse();
        response.setId(prescription.getIdMedicationPrescription());
        response.setAppointmentId(prescription.getAppointment().getIdAppointment());
        response.setMedicationId(prescription.getMedication().getIdMedication());
        response.setMedicationName(prescription.getMedication().getTitle());
        response.setDuration(prescription.getDuration());
        response.setInstructions(prescription.getInstructions());
        response.setStatus(prescription.getStatus().getTitle());
        response.setCreatedAt(prescription.getCreatedAt());
        return response;
    }

    private ProcedurePrescriptionResponse mapProcedureToResponse(ProcedurePrescription prescription) {
        ProcedurePrescriptionResponse response = new ProcedurePrescriptionResponse();
        response.setId(prescription.getIdProcedurePrescription());
        response.setAppointmentId(prescription.getAppointment().getIdAppointment());
        response.setProcedureId(prescription.getProcedure().getIdProcedure());
        response.setProcedureName(prescription.getProcedure().getTitle());
        response.setInstructions(prescription.getInstructions());
        response.setStatus(prescription.getStatus().getTitle());
        response.setCreatedAt(prescription.getCreatedAt());
        return response;
    }

    private AnalysisPrescriptionResponse mapAnalysisToResponse(AnalysisPrescription prescription) {
        AnalysisPrescriptionResponse response = new AnalysisPrescriptionResponse();
        response.setId(prescription.getIdAnalysisPrescription());
        response.setAppointmentId(prescription.getAppointment().getIdAppointment());
        response.setAnalysisId(prescription.getAnalysis().getIdAnalysis());
        response.setAnalysisName(prescription.getAnalysis().getTitle());
        response.setInstructions(prescription.getInstructions());
        response.setStatus(prescription.getStatus().getTitle());
        response.setCreatedAt(prescription.getCreatedAt());
        return response;
    }

    @Transactional
    public void deleteMedicationPrescription(String userEmail, Integer prescriptionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        MedicationPrescription prescription = medicationPrescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Назначение не найдено"));

        if (!prescription.getAppointment().getSlot().getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new UnauthorizedException("Вы не можете удалить это назначение");
        }

        // Check if appointment is cancelled
        if (prescription.getAppointment().getSlot().getCancellationReason() != null) {
            throw new IllegalStateException("Невозможно удалить назначение из отмененного приема");
        }

        medicationPrescriptionRepository.delete(prescription);
    }

    @Transactional
    public void deleteProcedurePrescription(String userEmail, Integer prescriptionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        ProcedurePrescription prescription = procedurePrescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Назначение не найдено"));

        if (!prescription.getAppointment().getSlot().getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new UnauthorizedException("Вы не можете удалить это назначение");
        }

        // Check if appointment is cancelled
        if (prescription.getAppointment().getSlot().getCancellationReason() != null) {
            throw new IllegalStateException("Невозможно удалить назначение из отмененного приема");
        }

        procedurePrescriptionRepository.delete(prescription);
    }

    @Transactional
    public void deleteAnalysisPrescription(String userEmail, Integer prescriptionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        AnalysisPrescription prescription = analysisPrescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Назначение не найдено"));

        if (!prescription.getAppointment().getSlot().getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new UnauthorizedException("Вы не можете удалить это назначение");
        }

        // Check if appointment is cancelled
        if (prescription.getAppointment().getSlot().getCancellationReason() != null) {
            throw new IllegalStateException("Невозможно удалить назначение из отмененного приема");
        }

        analysisPrescriptionRepository.delete(prescription);
    }
}
