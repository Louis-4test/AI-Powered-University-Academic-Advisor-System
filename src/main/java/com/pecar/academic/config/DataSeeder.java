package com.pecar.academic.config;

import com.pecar.academic.entity.*;
import com.pecar.academic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Populates the database with realistic demo data on application startup.
 *
 * Controlled by the {@code app.seed.enabled} property (default: true).
 * Set {@code app.seed.enabled=false} (e.g. in a "prod" profile) to skip seeding
 * entirely. The seeder also skips itself automatically if departments already
 * exist, so it is safe to leave enabled across repeated restarts in dev.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final UserRepository       userRepository;
    private final StudentRepository    studentRepository;
    private final LecturerRepository   lecturerRepository;
    private final CourseRepository     courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository      gradeRepository;
    private final PasswordEncoder      passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    private static final String ACADEMIC_YEAR = "2025/2026";
    private static final Random RANDOM = new Random(42); // fixed seed → reproducible demo data

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Data seeding disabled (app.seed.enabled=false). Skipping.");
            return;
        }

        if (departmentRepository.count() > 0) {
            log.info("Data already present ({} departments found). Skipping seeding.", departmentRepository.count());
            return;
        }

        log.info("Seeding demo data...");

        seedAdmin();
        List<Department> departments = seedDepartments();
        List<Lecturer> lecturers = seedLecturers(departments);
        List<Course> courses = seedCourses(departments, lecturers);
        List<Student> students = seedStudents(departments);
        seedEnrollmentsAndGrades(students, courses);

        log.info("Seeding complete: {} departments, {} lecturers, {} courses, {} students.",
                departments.size(), lecturers.size(), courses.size(), students.size());
        log.info("Demo login → admin@pecar.edu / Admin@123 (ADMIN)");
        log.info("Demo login → any seeded student/lecturer email / Pass@123");
    }

    // ── Admin account ─────────────────────────────────────────────────────────

    private void seedAdmin() {
        if (userRepository.existsByEmail("admin@pecar.edu")) return;

        User admin = User.builder()
                .email("admin@pecar.edu")
                .password(passwordEncoder.encode("Admin@123"))
                .fullName("System Administrator")
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);
    }

    // ── Departments ────────────────────────────────────────────────────────────

    private List<Department> seedDepartments() {
        List<Department> departments = List.of(
                Department.builder().name("Computer Science").code("CS")
                        .description("Software engineering, AI, and systems").build(),
                Department.builder().name("Electrical Engineering").code("EE")
                        .description("Power systems, electronics, and control").build(),
                Department.builder().name("Business Administration").code("BA")
                        .description("Management, finance, and entrepreneurship").build(),
                Department.builder().name("Mathematics").code("MTH")
                        .description("Pure and applied mathematics").build()
        );
        return departmentRepository.saveAll(departments);
    }

    // ── Lecturers ──────────────────────────────────────────────────────────────

    private List<Lecturer> seedLecturers(List<Department> departments) {
        Department cs  = byCode(departments, "CS");
        Department ee  = byCode(departments, "EE");
        Department ba  = byCode(departments, "BA");
        Department mth = byCode(departments, "MTH");

        record LecturerSeed(String first, String last, String email, String qualification,
                             String specialization, Department dept) {}

        List<LecturerSeed> seeds = List.of(
                new LecturerSeed("Yongho", "Louis", "y.louis@pecar.edu", "PhD",
                        "Software Engineering", cs),
                new LecturerSeed("Amara",  "Ngozi", "a.ngozi@pecar.edu", "PhD",
                        "Artificial Intelligence", cs),
                new LecturerSeed("Tabi",   "Etienne", "t.etienne@pecar.edu", "MSc",
                        "Database Systems", cs),
                new LecturerSeed("Brenda", "Folefac", "b.folefac@pecar.edu", "PhD",
                        "Power Systems", ee),
                new LecturerSeed("Samuel", "Achu", "s.achu@pecar.edu", "MSc",
                        "Control Systems", ee),
                new LecturerSeed("Clarisse", "Mballa", "c.mballa@pecar.edu", "PhD",
                        "Finance", ba),
                new LecturerSeed("Derrick", "Tanyi", "d.tanyi@pecar.edu", "MBA",
                        "Entrepreneurship", ba),
                new LecturerSeed("Patience", "Fon", "p.fon@pecar.edu", "PhD",
                        "Applied Mathematics", mth)
        );

        List<Lecturer> lecturers = new ArrayList<>();
        int counter = 1;
        for (LecturerSeed s : seeds) {
            User user = User.builder()
                    .email(s.email())
                    .password(passwordEncoder.encode("Pass@123"))
                    .fullName(s.first() + " " + s.last())
                    .role(Role.LECTURER)
                    .build();
            userRepository.save(user);

            Lecturer lecturer = Lecturer.builder()
                    .lecturerId(String.format("LEC-2025-%03d", counter++))
                    .firstName(s.first())
                    .lastName(s.last())
                    .email(s.email())
                    .phone(randomPhone())
                    .qualification(s.qualification())
                    .specialization(s.specialization())
                    .department(s.dept())
                    .user(user)
                    .build();
            lecturers.add(lecturer);
        }

        return lecturerRepository.saveAll(lecturers);
    }

    // ── Courses ────────────────────────────────────────────────────────────────

    private List<Course> seedCourses(List<Department> departments, List<Lecturer> lecturers) {
        Department cs  = byCode(departments, "CS");
        Department ee  = byCode(departments, "EE");
        Department ba  = byCode(departments, "BA");
        Department mth = byCode(departments, "MTH");

        Lecturer louis    = byEmail(lecturers, "y.louis@pecar.edu");
        Lecturer ngozi    = byEmail(lecturers, "a.ngozi@pecar.edu");
        Lecturer etienne  = byEmail(lecturers, "t.etienne@pecar.edu");
        Lecturer folefac  = byEmail(lecturers, "b.folefac@pecar.edu");
        Lecturer achu     = byEmail(lecturers, "s.achu@pecar.edu");
        Lecturer mballa   = byEmail(lecturers, "c.mballa@pecar.edu");
        Lecturer tanyi    = byEmail(lecturers, "d.tanyi@pecar.edu");
        Lecturer fon      = byEmail(lecturers, "p.fon@pecar.edu");

        record CourseSeed(String code, String title, String desc, int credits, int level,
                           String semester, Department dept, Lecturer lecturer) {}

        List<CourseSeed> seeds = List.of(
                new CourseSeed("CS101", "Introduction to Programming",
                        "Fundamentals of programming using Java", 4, 100, "FIRST", cs, louis),
                new CourseSeed("CS201", "Data Structures and Algorithms",
                        "Core data structures, complexity analysis", 4, 200, "FIRST", cs, etienne),
                new CourseSeed("CS301", "Object-Oriented Software Engineering",
                        "OOP design, patterns, and large-scale system architecture", 4, 300, "FIRST", cs, louis),
                new CourseSeed("CS302", "Database Management Systems",
                        "Relational design, SQL, and transactions", 3, 300, "SECOND", cs, etienne),
                new CourseSeed("CS401", "Artificial Intelligence",
                        "Search, ML fundamentals, and intelligent agents", 4, 400, "FIRST", cs, ngozi),
                new CourseSeed("CS402", "Machine Learning",
                        "Supervised/unsupervised learning and model evaluation", 4, 400, "SECOND", cs, ngozi),
                new CourseSeed("EE101", "Circuit Theory",
                        "DC/AC circuit analysis fundamentals", 4, 100, "FIRST", ee, folefac),
                new CourseSeed("EE301", "Control Systems Engineering",
                        "Feedback systems, stability, and controller design", 3, 300, "SECOND", ee, achu),
                new CourseSeed("BA101", "Principles of Management",
                        "Foundations of organizational management", 3, 100, "FIRST", ba, tanyi),
                new CourseSeed("BA301", "Corporate Finance",
                        "Capital budgeting, valuation, and financial strategy", 3, 300, "FIRST", ba, mballa),
                new CourseSeed("MTH201", "Linear Algebra",
                        "Vector spaces, matrices, and linear transformations", 3, 200, "FIRST", mth, fon),
                new CourseSeed("MTH301", "Probability and Statistics",
                        "Probability theory and statistical inference", 3, 300, "SECOND", mth, fon)
        );

        List<Course> courses = seeds.stream().map(s -> Course.builder()
                .courseCode(s.code())
                .title(s.title())
                .description(s.desc())
                .creditHours(s.credits())
                .level(s.level())
                .semester(s.semester())
                .department(s.dept())
                .lecturer(s.lecturer())
                .build()).toList();

        return courseRepository.saveAll(courses);
    }

    // ── Students ───────────────────────────────────────────────────────────────

    private List<Student> seedStudents(List<Department> departments) {
        Department cs  = byCode(departments, "CS");
        Department ee  = byCode(departments, "EE");
        Department ba  = byCode(departments, "BA");
        Department mth = byCode(departments, "MTH");

        record StudentSeed(String first, String last, String email, int level,
                            int enrollYear, String program, Department dept) {}

        List<StudentSeed> seeds = List.of(
                new StudentSeed("Fola",     "Louis",   "fola.louis@students.pecar.edu",   300, 2023, "B.Sc. Computer Science", cs),
                new StudentSeed("Ariane",   "Besong",  "ariane.besong@students.pecar.edu", 300, 2023, "B.Sc. Computer Science", cs),
                new StudentSeed("Junior",   "Ekema",   "junior.ekema@students.pecar.edu",  400, 2022, "B.Sc. Computer Science", cs),
                new StudentSeed("Hilary",   "Nkemzi",  "hilary.nkemzi@students.pecar.edu", 200, 2024, "B.Sc. Computer Science", cs),
                new StudentSeed("Gisele",   "Atangana","gisele.atangana@students.pecar.edu",100, 2025, "B.Sc. Computer Science", cs),
                new StudentSeed("Marcel",   "Ojong",   "marcel.ojong@students.pecar.edu",  300, 2023, "B.Eng. Electrical Engineering", ee),
                new StudentSeed("Linda",    "Ayuk",    "linda.ayuk@students.pecar.edu",    200, 2024, "B.Eng. Electrical Engineering", ee),
                new StudentSeed("Desmond",  "Fru",     "desmond.fru@students.pecar.edu",   400, 2022, "B.Eng. Electrical Engineering", ee),
                new StudentSeed("Carine",   "Mbarga",  "carine.mbarga@students.pecar.edu", 300, 2023, "B.Sc. Business Administration", ba),
                new StudentSeed("Roland",   "Suh",     "roland.suh@students.pecar.edu",    200, 2024, "B.Sc. Business Administration", ba),
                new StudentSeed("Brigitte", "Wanji",   "brigitte.wanji@students.pecar.edu", 100, 2025, "B.Sc. Business Administration", ba),
                new StudentSeed("Eric",     "Tchoua",  "eric.tchoua@students.pecar.edu",   300, 2023, "B.Sc. Mathematics", mth),
                new StudentSeed("Nadia",    "Epote",   "nadia.epote@students.pecar.edu",   200, 2024, "B.Sc. Mathematics", mth),
                new StudentSeed("Patrick",  "Ndip",    "patrick.ndip@students.pecar.edu",  400, 2022, "B.Sc. Computer Science", cs),
                new StudentSeed("Yvonne",   "Atemkeng","yvonne.atemkeng@students.pecar.edu",100, 2025, "B.Sc. Mathematics", mth)
        );

        List<Student> students = new ArrayList<>();
        int counter = 1;
        for (StudentSeed s : seeds) {
            User user = User.builder()
                    .email(s.email())
                    .password(passwordEncoder.encode("Pass@123"))
                    .fullName(s.first() + " " + s.last())
                    .role(Role.STUDENT)
                    .build();
            userRepository.save(user);

            Student student = Student.builder()
                    .studentId(String.format("STU-2025-%03d", counter++))
                    .firstName(s.first())
                    .lastName(s.last())
                    .email(s.email())
                    .phone(randomPhone())
                    .dateOfBirth(randomDob())
                    .enrollmentYear(s.enrollYear())
                    .currentLevel(s.level())
                    .programName(s.program())
                    .department(s.dept())
                    .user(user)
                    .build();
            students.add(student);
        }

        return studentRepository.saveAll(students);
    }

    // ── Enrollments + Grades ───────────────────────────────────────────────────

    private void seedEnrollmentsAndGrades(List<Student> students, List<Course> courses) {
        for (Student student : students) {
            List<Course> matching = courses.stream()
                    .filter(c -> c.getDepartment().getId().equals(student.getDepartment().getId()))
                    .filter(c -> c.getLevel() <= student.getCurrentLevel())
                    .toList();

            for (Course course : matching) {
                Enrollment enrollment = Enrollment.builder()
                        .student(student)
                        .course(course)
                        .academicYear(ACADEMIC_YEAR)
                        .semester(course.getSemester())
                        .status(Enrollment.EnrollmentStatus.ENROLLED)
                        .build();
                enrollmentRepository.save(enrollment);

                // Most students perform reasonably well; a few are deliberately
                // weaker so risk/at-risk queries have something to surface.
                boolean strugglingProfile = RANDOM.nextInt(10) < 2; // ~20% of pairings

                Grade grade = Grade.builder()
                        .student(student)
                        .course(course)
                        .academicYear(ACADEMIC_YEAR)
                        .semester(course.getSemester())
                        .attendance(strugglingProfile ? randomScore(40, 70) : randomScore(75, 100))
                        .assignments(strugglingProfile ? randomScore(35, 65) : randomScore(70, 98))
                        .projects(strugglingProfile ? randomScore(30, 60) : randomScore(65, 97))
                        .tests(strugglingProfile ? randomScore(30, 55) : randomScore(60, 95))
                        .exams(strugglingProfile ? randomScore(25, 50) : randomScore(55, 95))
                        .build();
                gradeRepository.save(grade);
            }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Department byCode(List<Department> departments, String code) {
        return departments.stream()
                .filter(d -> d.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Department " + code + " not seeded"));
    }

    private Lecturer byEmail(List<Lecturer> lecturers, String email) {
        return lecturers.stream()
                .filter(l -> l.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Lecturer " + email + " not seeded"));
    }

    private double randomScore(int min, int max) {
        return min + RANDOM.nextDouble() * (max - min);
    }

    private String randomPhone() {
        return String.format("+237 6%02d %03d %03d",
                RANDOM.nextInt(100), RANDOM.nextInt(1000), RANDOM.nextInt(1000));
    }

    private LocalDate randomDob() {
        int year = 1999 + RANDOM.nextInt(7); // 1999–2005
        int month = 1 + RANDOM.nextInt(12);
        int day = 1 + RANDOM.nextInt(28);
        return LocalDate.of(year, month, day);
    }
}
