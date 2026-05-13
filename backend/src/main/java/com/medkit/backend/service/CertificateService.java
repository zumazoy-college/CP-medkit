package com.medkit.backend.service;

import com.medkit.backend.dto.request.CreateCertificateRequest;
import com.medkit.backend.dto.response.CertificateResponse;
import com.medkit.backend.entity.*;
import com.medkit.backend.exception.BadRequestException;
import com.medkit.backend.exception.ResourceNotFoundException;
import com.medkit.backend.exception.UnauthorizedException;
import com.medkit.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentDiagnosisRepository appointmentDiagnosisRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PdfGeneratorService pdfGeneratorService;

    @Value("${file.upload-dir:uploads/certificates}")
    private String uploadDir;

    @Transactional
    public CertificateResponse createCertificate(String userEmail, CreateCertificateRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        if (!appointment.getSlot().getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
            throw new UnauthorizedException("Вы не можете создать справку для приема другого врача");
        }

        if (!"completed".equals(appointment.getSlot().getStatus().getTitle())) {
            throw new BadRequestException("Справку можно создать только для завершенного приема");
        }

        if (!request.getCertificateType().equals("visit") && !request.getCertificateType().equals("work_study")) {
            throw new BadRequestException("Неверный тип справки");
        }

        List<AppointmentDiagnosis> diagnoses = appointmentDiagnosisRepository
                .findByAppointment_IdAppointment(appointment.getIdAppointment());

        byte[] pdfBytes;
        try {
            if ("visit".equals(request.getCertificateType())) {
                pdfBytes = pdfGeneratorService.generateVisitCertificate(
                        appointment,
                        appointment.getPatient(),
                        doctor,
                        diagnoses,
                        request.getValidFrom(),
                        request.getValidTo()
                );
            } else {
                pdfBytes = pdfGeneratorService.generateWorkStudyCertificate(
                        appointment,
                        appointment.getPatient(),
                        doctor,
                        diagnoses,
                        request.getValidFrom(),
                        request.getValidTo(),
                        request.getDisabilityPeriodFrom(),
                        request.getDisabilityPeriodTo(),
                        request.getWorkRestrictions()
                );
            }
        } catch (IOException e) {
            log.error("Ошибка генерации PDF", e);
            throw new RuntimeException("Ошибка генерации справки");
        }

        String fileName = UUID.randomUUID().toString() + ".pdf";
        String filePath = savePdfFile(pdfBytes, fileName);

        Certificate certificate = new Certificate();
        certificate.setAppointment(appointment);
        certificate.setPatient(appointment.getPatient());
        certificate.setDoctor(doctor);
        certificate.setCertificateType(request.getCertificateType());
        certificate.setFilePath(filePath);
        certificate.setValidFrom(request.getValidFrom());
        certificate.setValidTo(request.getValidTo());
        certificate.setDisabilityPeriodFrom(request.getDisabilityPeriodFrom());
        certificate.setDisabilityPeriodTo(request.getDisabilityPeriodTo());
        certificate.setWorkRestrictions(request.getWorkRestrictions());

        certificate = certificateRepository.save(certificate);

        return mapToResponse(certificate);
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> getPatientCertificates(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));

        List<Certificate> certificates = certificateRepository
                .findByPatient_IdPatientOrderByCreatedAtDesc(patient.getIdPatient());

        return certificates.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> getAppointmentCertificates(String userEmail, Integer appointmentId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Прием не найден"));

        if ("doctor".equals(user.getRole())) {
            Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                    .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));
            if (!appointment.getSlot().getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
                throw new UnauthorizedException("Доступ запрещен");
            }
        } else if ("patient".equals(user.getRole())) {
            Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                    .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));
            if (!appointment.getPatient().getIdPatient().equals(patient.getIdPatient())) {
                throw new UnauthorizedException("Доступ запрещен");
            }
        }

        List<Certificate> certificates = certificateRepository
                .findByAppointment_IdAppointment(appointmentId);

        return certificates.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] downloadCertificate(String userEmail, Integer certificateId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Справка не найдена"));

        if ("doctor".equals(user.getRole())) {
            Doctor doctor = doctorRepository.findByUser_IdUser(user.getIdUser())
                    .orElseThrow(() -> new ResourceNotFoundException("Врач не найден"));
            if (!certificate.getDoctor().getIdDoctor().equals(doctor.getIdDoctor())) {
                throw new UnauthorizedException("Доступ запрещен");
            }
        } else if ("patient".equals(user.getRole())) {
            Patient patient = patientRepository.findByUser_IdUser(user.getIdUser())
                    .orElseThrow(() -> new ResourceNotFoundException("Пациент не найден"));
            if (!certificate.getPatient().getIdPatient().equals(patient.getIdPatient())) {
                throw new UnauthorizedException("Доступ запрещен");
            }
        }

        try {
            Path filePath = Paths.get(certificate.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Ошибка чтения файла справки", e);
            throw new RuntimeException("Ошибка загрузки справки");
        }
    }

    private String savePdfFile(byte[] pdfBytes, String fileName) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, pdfBytes);

            return filePath.toString();
        } catch (IOException e) {
            log.error("Ошибка сохранения PDF файла", e);
            throw new RuntimeException("Ошибка сохранения справки");
        }
    }

    private CertificateResponse mapToResponse(Certificate certificate) {
        CertificateResponse response = new CertificateResponse();
        response.setIdCertificate(certificate.getIdCertificate());
        response.setAppointmentId(certificate.getAppointment().getIdAppointment());
        response.setPatientId(certificate.getPatient().getIdPatient());
        response.setPatientFullName(getPatientFullName(certificate.getPatient()));
        response.setDoctorId(certificate.getDoctor().getIdDoctor());
        response.setDoctorFullName(getDoctorFullName(certificate.getDoctor()));
        response.setDoctorSpecialization(certificate.getDoctor().getSpecialization());
        response.setCertificateType(certificate.getCertificateType());
        response.setCertificateTypeName(getCertificateTypeName(certificate.getCertificateType()));
        response.setFilePath(certificate.getFilePath());
        response.setValidFrom(certificate.getValidFrom());
        response.setValidTo(certificate.getValidTo());
        response.setDisabilityPeriodFrom(certificate.getDisabilityPeriodFrom());
        response.setDisabilityPeriodTo(certificate.getDisabilityPeriodTo());
        response.setWorkRestrictions(certificate.getWorkRestrictions());
        response.setCreatedAt(certificate.getCreatedAt());
        return response;
    }

    private String getPatientFullName(Patient patient) {
        StringBuilder name = new StringBuilder();
        name.append(patient.getUser().getLastName()).append(" ");
        name.append(patient.getUser().getFirstName());
        if (patient.getUser().getMiddleName() != null) {
            name.append(" ").append(patient.getUser().getMiddleName());
        }
        return name.toString();
    }

    private String getDoctorFullName(Doctor doctor) {
        StringBuilder name = new StringBuilder();
        name.append(doctor.getUser().getLastName()).append(" ");
        name.append(doctor.getUser().getFirstName());
        if (doctor.getUser().getMiddleName() != null) {
            name.append(" ").append(doctor.getUser().getMiddleName());
        }
        return name.toString();
    }

    private String getCertificateTypeName(String type) {
        return switch (type) {
            case "visit" -> "Справка о посещении врача";
            case "work_study" -> "Справка для работы/учебы (095/у)";
            default -> type;
        };
    }
}
