package com.tamjid.course.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tamjid.course.dto.CourseDTO;
import com.tamjid.course.exception.ResourceNotFoundException;
import com.tamjid.course.model.Course;
import com.tamjid.course.repository.CourseRepository;
import com.tamjid.course.service.CourseService;

/**
 * Implementation of the CourseService interface.
 *
 * This is where the actual business logic for course operations lives.
 *
 * POSITION IN THE ARCHITECTURE:
 *   Controller ──calls──► CourseServiceImpl ──calls──► CourseRepository ──SQL──► Oracle DB
 *       ▲                        │
 *       │                        ├── Converts CourseDTO ↔ Course entity (so the controller
 *       │                        │   never touches JPA entities directly)
 *       │                        └── Throws ResourceNotFoundException when a course is missing
 *       │                            (GlobalExceptionHandler turns it into a 404 HTTP response)
 *       │
 *       └── Returns CourseDTO (JSON response to the frontend)
 *
 * @Service tells Spring:
 *   "This class contains business logic — create a bean from it and make it available
 *    for dependency injection." Spring will register an instance of this class in its
 *    IoC container at startup, so CourseController can inject it via its constructor.
 */
@Service
public class CourseServiceImpl implements CourseService {

    // The repository handles direct database communication (CRUD on the Course entity).
    // Spring injects a proxy implementation of CourseRepository at runtime.
    private final CourseRepository repository;

    // Constructor injection — Spring provides the CourseRepository bean automatically.
    // We mark the field as 'final' to ensure it's assigned exactly once (immutability).
    public CourseServiceImpl(CourseRepository repository) {
        this.repository = repository;
    }

    /**
     * Fetches all courses from the database and converts each one to a DTO.
     *
     * HOW IT WORKS:
     *   repository.findAll()  → returns List<Course> (JPA entities from the DB)
     *   .stream()             → converts the list to a Java Stream for functional processing
     *   .map(this::convertToDTO) → transforms each Course entity into a CourseDTO
     *   .collect(Collectors.toList()) → gathers the results back into a List<CourseDTO>
     *
     * WHY STREAMS?
     * Streams provide a clean, declarative way to transform collections.
     * The alternative (a for-loop with manual list building) would be more verbose.
     */
    @Override
    public List<CourseDTO> getAllCourses() {
        return repository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds a course by ID. If it doesn't exist, throws ResourceNotFoundException.
     *
     * WHY orElseThrow() INSTEAD OF orElse(null)?
     *   - Returning null would force the controller to deal with null checks everywhere.
     *   - Throwing an exception makes the error explicit and is handled by GlobalExceptionHandler,
     *     which returns a clean 404 JSON response to the frontend.
     *
     * FLOW WHEN COURSE EXISTS:
     *   findById(id) → Optional containing the Course → orElseThrow returns the Course
     *   → convertToDTO → returned to controller
     *
     * FLOW WHEN COURSE DOESN'T EXIST:
     *   findById(id) → empty Optional → orElseThrow executes the lambda
     *   → throws ResourceNotFoundException → GlobalExceptionHandler catches it
     *   → returns HTTP 404 with error message
     */
    @Override
    public CourseDTO getCourseById(Long id) {
        Course course = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found: " + id));

        return convertToDTO(course);
    }

    /**
     * Creates a new course and saves it to the database.
     *
     * FLOW:
     *   1. convertToEntity(dto) → transforms the incoming DTO into a Course JPA entity
     *   2. repository.save(course) → Hibernate generates an INSERT SQL statement.
     *      Before inserting, Hibernate fetches the next value from Oracle's S_TAMJID_COURSES
     *      sequence and sets it as courseNo.
     *   3. convertToDTO(savedCourse) → transforms the saved entity (now with a generated ID)
     *      back into a DTO to return to the frontend.
     *
     * The returned DTO includes the auto-generated courseNo so the frontend knows
     * the ID of the newly created course.
     */
    @Override
    public CourseDTO createCourse(CourseDTO dto) {
        Course course = convertToEntity(dto);
        Course savedCourse = repository.save(course);
        return convertToDTO(savedCourse);
    }

    /**
     * Updates an existing course.
     *
     * WHY DO WE FETCH FIRST INSTEAD OF JUST SAVING?
     *   If we just called save() with a new Course object, Hibernate would overwrite ALL columns,
     *   including ones we didn't intend to change (potentially setting them to null).
     *   By fetching the existing entity first, we only update the specific fields we want
     *   (courseName, courseDescription, courseFee), leaving everything else intact.
     *
     * FLOW:
     *   1. Find the existing course by ID (throw 404 if not found).
     *   2. Update the fields on the existing entity with values from the DTO.
     *   3. Save the modified entity back to the database (Hibernate generates an UPDATE SQL).
     *   4. Convert and return the updated entity as a DTO.
     */
    @Override
    public CourseDTO updateCourse(Long id, CourseDTO dto) {
        Course existingCourse = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found: " + id));

        // Update only the mutable fields — courseNo is NOT updated (it's the primary key).
        existingCourse.setCourseName(dto.getCourseName());
        existingCourse.setCourseDescription(dto.getCourseDescription());
        existingCourse.setCourseFee(dto.getCourseFee());

        // JPA's "dirty checking" detects that the entity has changed and generates an UPDATE.
        Course updatedCourse = repository.save(existingCourse);

        return convertToDTO(updatedCourse);
    }

    /**
     * Deletes a course from the database.
     * Hibernate generates: DELETE FROM TAMJID_COURSES WHERE COURSE_NO = ?
     *
     * Note: This does NOT check if the course exists first. If the ID doesn't exist,
     * the DELETE simply affects 0 rows and no exception is thrown.
     * (You could add an existence check here if your business rules require it.)
     */
    @Override
    public void deleteCourse(Long id) {
        repository.deleteById(id);
    }


    // ===========================
    //  DTO ↔ Entity Converters
    // ===========================

    /**
     * Converts a Course entity (from the database) into a CourseDTO (for the frontend).
     *
     * WHY NOT JUST RETURN THE ENTITY?
     *   - The Course entity has JPA annotations and internal Hibernate state (proxies,
     *     lazy-loading metadata, etc.) that shouldn't leak into the API response.
     *   - Using a DTO gives us full control over which fields the frontend sees.
     *   - If we ever add sensitive fields to Course (e.g., internal notes, audit logs),
     *     we can simply exclude them from the DTO.
     */
    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();

        dto.setCourseNo(course.getCourseNo());
        dto.setCourseName(course.getCourseName());
        dto.setCourseDescription(course.getCourseDescription());
        dto.setCourseFee(course.getCourseFee());

        return dto;
    }

    /**
     * Converts a CourseDTO (from the frontend) into a Course entity (for the database).
     *
     * This is the reverse of convertToDTO. It's used when creating a new course —
     * the frontend sends JSON, Spring deserializes it into a CourseDTO, and this
     * method transforms it into a JPA entity that Hibernate can persist.
     */
    private Course convertToEntity(CourseDTO dto) {
        Course course = new Course();

        course.setCourseNo(dto.getCourseNo());
        course.setCourseName(dto.getCourseName());
        course.setCourseDescription(dto.getCourseDescription());
        course.setCourseFee(dto.getCourseFee());

        return course;
    }
}