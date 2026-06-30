package com.pecar.academic.service;

import com.pecar.academic.dto.LecturerDTO;
import com.pecar.academic.dto.LecturerStudentDTO;
import com.pecar.academic.entity.*;
import com.pecar.academic.exception.DuplicateResourceException;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.CourseRepository;
import com.pecar.academic.repository.DepartmentRepository;
import com.pecar.academic.repository.EnrollmentRepository;
import com.pecar.academic.repository.LecturerRepository;
import com.pecar.academic.repository.StudentRepository;
import com.pecar.academic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LecturerService {

    private final LecturerRepository   lecturerRepository;
    private final UserRepository       userRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository     courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository    studentRepository;
    private final PasswordEncoder      passwordEncoder;

    @Transactional
    public LecturerDTO.Response create(LecturerDTO.Request req) {
        if (lecturerRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("Lecturer with email " + req.getEmail() + " already exists");
        }

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFirstName() + " " + req.getLastName())
                .role(Role.LECTURER)
                .build();
        userRepository.save(user);

        Department dept = req.getDepartmentId() != null
                ? departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"))
                : null;

        Lecturer lecturer = Lecturer.builder()
                .lecturerId(generateLecturerId())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .qualification(req.getQualification())
                .specialization(req.getSpecialization())
                .department(dept)
                .user(user)
                .build();

        return toResponse(lecturerRepository.save(lecturer));
    }

    public LecturerDTO.Response getById(Long id) {
        return toResponse(findById(id));
    }

    public LecturerDTO.Response getByEmail(String email) {
        return toResponse(lecturerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No lecturer record found for " + email)));
    }

    public List<LecturerDTO.Response> getAll() {
        return lecturerRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<LecturerDTO.Response> search(String keyword) {
        return lecturerRepository.searchLecturers(keyword).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public LecturerDTO.Response update(Long id, LecturerDTO.UpdateRequest req) {
        Lecturer lecturer = findById(id);

        if (!lecturer.getEmail().equals(req.getEmail()) && lecturerRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("Email already in use");
        }

        lecturer.setFirstName(req.getFirstName());
        lecturer.setLastName(req.getLastName());
        lecturer.setEmail(req.getEmail());
        lecturer.setPhone(req.getPhone());
        lecturer.setQualification(req.getQualification());
        lecturer.setSpecialization(req.getSpecialization());

        if (req.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            lecturer.setDepartment(dept);
        } else {
            lecturer.setDepartment(null);
        }

        // Keep the linked login account's name/email in sync with the lecturer record.
        if (lecturer.getUser() != null) {
            lecturer.getUser().setFullName(req.getFirstName() + " " + req.getLastName());
            lecturer.getUser().setEmail(req.getEmail());
            userRepository.save(lecturer.getUser());
        }

        return toResponse(lecturerRepository.save(lecturer));
    }

    @Transactional
    public void delete(Long id) {
        Lecturer lecturer = findById(id);

        // Deleting a lecturer must never delete the courses they taught —
        // unassign the lecturer from each course instead.
        lecturer.getCourses().forEach(c -> c.setLecturer(null));

        lecturerRepository.delete(lecturer);
    }

    public List<LecturerStudentDTO> getStudentsByLecturerEmail(String email) {
        Lecturer lecturer = lecturerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No lecturer record found for " + email));

        List<Course> courses = courseRepository.findByLecturerId(lecturer.getId());

        if (courses.isEmpty()) return List.of();

        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdIn(courseIds);

        return enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                .map(e -> LecturerStudentDTO.builder()
                        .studentId(e.getStudent().getId())
                        .studentNumber(e.getStudent().getStudentId())
                        .fullName(e.getStudent().getFullName())
                        .email(e.getStudent().getEmail())
                        .currentLevel(e.getStudent().getCurrentLevel())
                        .programName(e.getStudent().getProgramName())
                        .departmentName(e.getStudent().getDepartment() != null
                                ? e.getStudent().getDepartment().getName() : null)
                        .courseId(e.getCourse().getId())
                        .courseCode(e.getCourse().getCourseCode())
                        .courseTitle(e.getCourse().getTitle())
                        .academicYear(e.getAcademicYear())
                        .semester(e.getSemester())
                        .build())
                .toList();
    }

    private Lecturer findById(Long id) {
        return lecturerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer not found"));
    }

    private String generateLecturerId() {
        int year = java.time.Year.now().getValue();
        long count = lecturerRepository.count() + 1;
        return String.format("LEC-%d-%03d", year, count);
    }

    private LecturerDTO.Response toResponse(Lecturer l) {
        return LecturerDTO.Response.builder()
                .id(l.getId())
                .lecturerId(l.getLecturerId())
                .firstName(l.getFirstName())
                .lastName(l.getLastName())
                .fullName(l.getFullName())
                .email(l.getEmail())
                .phone(l.getPhone())
                .qualification(l.getQualification())
                .specialization(l.getSpecialization())
                .departmentId(l.getDepartment() != null ? l.getDepartment().getId() : null)
                .departmentName(l.getDepartment() != null ? l.getDepartment().getName() : null)
                .courseCount(l.getCourses().size())
                .build();
    }
}
