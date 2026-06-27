package com.pecar.academic.service;

import com.pecar.academic.dto.TimetableDTO;
import com.pecar.academic.entity.*;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

    private final TimetableRepository  timetableRepository;
    private final CourseRepository     courseRepository;
    private final LecturerRepository   lecturerRepository;
    private final DepartmentRepository departmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository    studentRepository;

    private static final LocalTime[][] TIME_SLOTS = {
        { LocalTime.of(8, 0),  LocalTime.of(9, 30)  },
        { LocalTime.of(9, 45), LocalTime.of(11, 15) },
        { LocalTime.of(11, 30), LocalTime.of(13, 0) },
        { LocalTime.of(14, 0), LocalTime.of(15, 30) },
        { LocalTime.of(15, 45), LocalTime.of(17, 15) },
    };

    private static final DayOfWeek[] DAYS = DayOfWeek.values();

    @Transactional
    public TimetableDTO.Response createEntry(TimetableDTO.Request req) {
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Lecturer lecturer = null;
        if (req.getLecturerId() != null) {
            lecturer = lecturerRepository.findById(req.getLecturerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lecturer not found"));
        }

        Department department = null;
        if (req.getDepartmentId() != null) {
            department = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        } else if (course.getDepartment() != null) {
            department = course.getDepartment();
        }

        LocalTime startTime = LocalTime.parse(req.getStartTime());
        LocalTime endTime = LocalTime.parse(req.getEndTime());

        TimetableEntry entry = TimetableEntry.builder()
                .course(course)
                .lecturer(lecturer)
                .dayOfWeek(req.getDayOfWeek())
                .startTime(startTime)
                .endTime(endTime)
                .room(req.getRoom())
                .location(req.getLocation())
                .department(department)
                .academicYear(req.getAcademicYear())
                .semester(req.getSemester())
                .build();

        return toResponse(timetableRepository.save(entry));
    }

    @Transactional
    public TimetableDTO.Response updateEntry(Long id, TimetableDTO.Request req) {
        TimetableEntry entry = findById(id);

        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Lecturer lecturer = null;
        if (req.getLecturerId() != null) {
            lecturer = lecturerRepository.findById(req.getLecturerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lecturer not found"));
        }

        Department department = null;
        if (req.getDepartmentId() != null) {
            department = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        }

        entry.setCourse(course);
        entry.setLecturer(lecturer);
        entry.setDayOfWeek(req.getDayOfWeek());
        entry.setStartTime(LocalTime.parse(req.getStartTime()));
        entry.setEndTime(LocalTime.parse(req.getEndTime()));
        entry.setRoom(req.getRoom());
        entry.setLocation(req.getLocation());
        entry.setDepartment(department);
        entry.setAcademicYear(req.getAcademicYear());
        entry.setSemester(req.getSemester());

        return toResponse(timetableRepository.save(entry));
    }

    @Transactional
    public void deleteEntry(Long id) {
        timetableRepository.delete(findById(id));
    }

    @Transactional
    public void deleteEntriesByCourse(Long courseId) {
        timetableRepository.deleteByCourseId(courseId);
    }

    public TimetableDTO.Response getEntryById(Long id) {
        return toResponse(findById(id));
    }

    public List<TimetableDTO.Response> getAllEntries() {
        return timetableRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<TimetableDTO.Response> getByDepartment(Long departmentId) {
        return timetableRepository.findByDepartmentId(departmentId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<TimetableDTO.Response> getByLecturer(Long lecturerId) {
        return timetableRepository.findByLecturerId(lecturerId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<TimetableDTO.Response> getByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Set<Long> enrolledCourseIds = enrollmentRepository.findByStudentId(studentId).stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toSet());

        return timetableRepository.findByDepartmentId(student.getDepartment().getId()).stream()
                .filter(e -> enrolledCourseIds.contains(e.getCourse().getId()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TimetableDTO.Response> getByCourse(Long courseId) {
        return timetableRepository.findByCourseId(courseId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<TimetableDTO.Response> generateTimetable(TimetableDTO.GenerateRequest req) {
        Department department = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        timetableRepository.findByDepartmentIdAndAcademicYearAndSemester(
                req.getDepartmentId(), req.getAcademicYear(), req.getSemester())
                .forEach(entry -> timetableRepository.delete(entry));

        List<Course> departmentCourses = courseRepository.findByDepartmentId(req.getDepartmentId()).stream()
                .filter(c -> c.getSemester() == null || c.getSemester().equals(req.getSemester()))
                .filter(c -> c.getLecturer() != null)
                .collect(Collectors.toList());

        if (departmentCourses.isEmpty()) return List.of();

        Map<Long, List<LocalTime[]>> lecturerSchedule = new HashMap<>();
        Map<DayOfWeek, Integer> daySlotIndex = new HashMap<>();
        List<TimetableEntry> generated = new ArrayList<>();

        int coursesPerDay = Math.max(1, (int) Math.ceil((double) departmentCourses.size() / DAYS.length));

        for (int i = 0; i < departmentCourses.size(); i++) {
            Course course = departmentCourses.get(i);
            int dayIdx = i / coursesPerDay % DAYS.length;
            DayOfWeek day = DAYS[dayIdx];

            int slotIdx = daySlotIndex.merge(day, 0, (old, v) -> old + 1) - 1;
            if (slotIdx >= TIME_SLOTS.length) slotIdx = slotIdx % TIME_SLOTS.length;

            LocalTime startTime = TIME_SLOTS[slotIdx][0];
            LocalTime endTime = TIME_SLOTS[slotIdx][1];

            TimetableEntry entry = TimetableEntry.builder()
                    .course(course)
                    .lecturer(course.getLecturer())
                    .dayOfWeek(day)
                    .startTime(startTime)
                    .endTime(endTime)
                    .room("Room " + (100 + (i % 20)))
                    .department(department)
                    .academicYear(req.getAcademicYear())
                    .semester(req.getSemester())
                    .build();

            generated.add(timetableRepository.save(entry));
        }

        return generated.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private TimetableEntry findById(Long id) {
        return timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable entry not found"));
    }

    private TimetableDTO.Response toResponse(TimetableEntry e) {
        return TimetableDTO.Response.builder()
                .id(e.getId())
                .courseId(e.getCourse().getId())
                .courseCode(e.getCourse().getCourseCode())
                .courseTitle(e.getCourse().getTitle())
                .lecturerId(e.getLecturer() != null ? e.getLecturer().getId() : null)
                .lecturerName(e.getLecturer() != null ? e.getLecturer().getFullName() : null)
                .dayOfWeek(e.getDayOfWeek())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .room(e.getRoom())
                .location(e.getLocation())
                .departmentId(e.getDepartment() != null ? e.getDepartment().getId() : null)
                .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
                .academicYear(e.getAcademicYear())
                .semester(e.getSemester())
                .build();
    }
}
