package com.tamjid.course.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tamjid.course.dto.CourseDTO;
import com.tamjid.course.service.CourseService;

/**
 * REST Controller that exposes CRUD endpoints for managing courses.
 *
 * HOW IT FITS IN THE ARCHITECTURE:
 *   Frontend (Angular) ──HTTP──► CourseController ──► CourseService ──► CourseRepository ──► Oracle DB
 *
 * This controller does NOT contain any business logic. It only:
 *   1. Receives HTTP requests from the frontend.
 *   2. Delegates the work to CourseService (the business logic layer).
 *   3. Returns the result as JSON (Spring converts CourseDTO objects automatically).
 *
 * ENDPOINTS EXPOSED:
 *   GET    /api/courses        → list all courses
 *   GET    /api/courses/{id}   → get a single course by its ID
 *   POST   /api/courses        → create a new course (expects JSON body)
 *   PUT    /api/courses/{id}   → update an existing course (expects JSON body)
 *   DELETE /api/courses/{id}   → delete a course
 */
@RestController // Tells Spring: this class handles HTTP requests and returns JSON responses.
@RequestMapping("/api/courses") // All endpoints in this controller start with /api/courses.
@CrossOrigin("*") // Allows requests from ANY origin (a fallback on top of CorsConfig).
public class CourseController {

    // We depend on the CourseService interface, NOT the implementation directly.
    // This is called "Dependency Inversion" — it makes the code easier to test
    // and swap implementations later if needed.
    // Spring automatically injects CourseServiceImpl here via constructor injection.
    private final CourseService courseService;

    // Constructor injection — Spring provides the CourseService bean at startup.
    // This is preferred over @Autowired on fields because it makes the dependency
    // explicit and allows the field to be final (immutable).
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * GET /api/courses
     * Returns a list of all courses. The Angular frontend calls this to
     * display the course catalog on its main page.
     */
    @GetMapping
    public List<CourseDTO> getAllCourses() {
        return courseService.getAllCourses();
    }

    /**
     * GET /api/courses/{id}
     * Returns a single course by its database ID (course_no).
     *
     * @PathVariable extracts the {id} from the URL path.
     * For example: GET /api/courses/5 → id = 5
     *
     * If the course doesn't exist, CourseServiceImpl throws ResourceNotFoundException,
     * which GlobalExceptionHandler catches and turns into a 404 response.
     */
    @GetMapping("/{id}")
    public CourseDTO getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    /**
     * POST /api/courses
     * Creates a new course. The Angular frontend sends a JSON body like:
     * { "courseName": "Java Basics", "courseDescription": "...", "courseFee": 500 }
     *
     * @RequestBody tells Spring to deserialize the incoming JSON into a CourseDTO object.
     * The courseNo (primary key) is NOT sent — Oracle's sequence generates it automatically.
     *
     * Returns the saved CourseDTO (including the generated courseNo) so the frontend
     * knows the ID of the newly created course.
     */
    @PostMapping
    public CourseDTO createCourse(@RequestBody CourseDTO courseDTO) {
        return courseService.createCourse(courseDTO);
    }

    /**
     * PUT /api/courses/{id}
     * Updates an existing course. The frontend sends the full updated course data
     * as JSON, and {id} in the URL tells us which course to update.
     *
     * Both @PathVariable (the id from the URL) and @RequestBody (the new data)
     * are needed here.
     */
    @PutMapping("/{id}")
    public CourseDTO updateCourse(
            @PathVariable Long id,
            @RequestBody CourseDTO courseDTO
    ) {
        return courseService.updateCourse(id, courseDTO);
    }

    /**
     * DELETE /api/courses/{id}
     * Deletes a course by its ID. Returns no body (HTTP 200 OK by default).
     * The Angular frontend typically removes the row from its table after
     * receiving a successful response.
     */
    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }
}