package com.pecar.academic.service;

import com.pecar.academic.dto.StudentDTO;
import com.pecar.academic.entity.*;
import com.pecar.academic.exception.DuplicateResourceException;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository  studentRepository;
    private final UserRepository     userRepository;
    private final DepartmentRepository departmentRepository;
    private final GradeRepository    gradeRepository;
    private final PasswordEncoder    passwordEncoder;

    // ── Create ─────────────────────────────────────────────────────────────────

    @Transactional
    public StudentDTO.Response createStudent(StudentDTO.Request req) {
        if (studentRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("Student with email " + req.getEmail() + " already exists");
        }

        // Create linked user account
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFirstName() + " " + req.getLastName())
                .role(Role.STUDENT)
                .build();
        userRepository.save(user);

        Department dept = req.getDepartmentId() != null
                ? departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"))
                : null;

        Student student = Student.builder()
                .studentId(generateStudentId())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .dateOfBirth(req.getDateOfBirth())
                .enrollmentYear(req.getEnrollmentYear())
                .currentLevel(req.getCurrentLevel())
                .programName(req.getProgramName())
                .department(dept)
                .user(user)
                .build();

        return toResponse(studentRepository.save(student));
    }

    // ── Read ───────────────────────────────────────────────────────────────────

    public StudentDTO.Response getStudentById(Long id) {
        return toResponse(findById(id));
    }

    public StudentDTO.Response getStudentByStudentId(String studentId) {
        return toResponse(studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student " + studentId + " not found")));
    }

    public StudentDTO.Response getStudentByEmail(String email) {
        return toResponse(studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No student record found for " + email)));
    }

    public List<StudentDTO.Summary> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public List<StudentDTO.Summary> searchStudents(String keyword) {
        return studentRepository.searchStudents(keyword).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    // ── Streams API showcase: high-achievers ───────────────────────────────────

    public List<StudentDTO.Summary> getHighAchievers(Double minCgpa) {
        return studentRepository.findAll().stream()
                .filter(s -> getCgpa(s.getId()) >= minCgpa)
                .sorted(Comparator.comparingDouble((Student s) -> getCgpa(s.getId())).reversed())
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getStudentCountByLevel() {
        return studentRepository.findAll().stream()
                .filter(s -> s.getCurrentLevel() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getCurrentLevel().getLabel(),
                        Collectors.counting()));
    }

    public Map<String, Long> getStatusDistribution() {
        return studentRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        s -> s.getStatus().name(),
                        Collectors.counting()));
    }

    // ── Update ─────────────────────────────────────────────────────────────────

    @Transactional
    public StudentDTO.Response updateStudent(Long id, StudentDTO.UpdateRequest req) {
        Student student = findById(id);

        if (!student.getEmail().equals(req.getEmail()) && studentRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("Email already in use");
        }

        student.setFirstName(req.getFirstName());
        student.setLastName(req.getLastName());
        student.setEmail(req.getEmail());
        student.setPhone(req.getPhone());
        student.setDateOfBirth(req.getDateOfBirth());
        student.setCurrentLevel(req.getCurrentLevel());
        student.setProgramName(req.getProgramName());

        if (req.getStatus() != null) {
            student.setStatus(req.getStatus());
        }

        if (req.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            student.setDepartment(dept);
        } else {
            student.setDepartment(null);
        }

        return toResponse(studentRepository.save(student));
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteStudent(Long id) {
        Student student = findById(id);
        if (student.getUser() != null) {
            userRepository.delete(student.getUser());
        }
        studentRepository.delete(student);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id " + id + " not found"));
    }

    private Double getCgpa(Long studentId) {
        return gradeRepository.calculateCGPA(studentId).orElse(0.0);
    }

    private String generateStudentId() {
        int year = java.time.Year.now().getValue();
        long count = studentRepository.count() + 1;
        return String.format("STU-%d-%03d", year, count);
    }

    // ── Mappers ────────────────────────────────────────────────────────────────

    private StudentDTO.Response toResponse(Student s) {
        return StudentDTO.Response.builder()
                .id(s.getId())
                .studentId(s.getStudentId())
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .fullName(s.getFullName())
                .email(s.getEmail())
                .phone(s.getPhone())
                .dateOfBirth(s.getDateOfBirth())
                .enrollmentYear(s.getEnrollmentYear())
                .currentLevel(s.getCurrentLevel())
                .programName(s.getProgramName())
                .status(s.getStatus())
                .departmentId(s.getDepartment() != null ? s.getDepartment().getId() : null)
                .departmentName(s.getDepartment() != null ? s.getDepartment().getName() : null)
                .cgpa(getCgpa(s.getId()))
                .createdAt(s.getCreatedAt())
                .build();
    }

    private StudentDTO.Summary toSummary(Student s) {
        return StudentDTO.Summary.builder()
                .id(s.getId())
                .studentId(s.getStudentId())
                .fullName(s.getFullName())
                .email(s.getEmail())
                .currentLevel(s.getCurrentLevel())
                .status(s.getStatus())
                .cgpa(getCgpa(s.getId()))
                .departmentId(s.getDepartment() != null ? s.getDepartment().getId() : null)
                .build();
    }
}
