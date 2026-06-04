package com.tamjid.course.exception;

/**
 * Custom exception thrown when a requested resource is not found in the database.
 *
 * WHY A CUSTOM EXCEPTION INSTEAD OF USING RuntimeException?
 *   1. Clarity — "ResourceNotFoundException" clearly communicates what went wrong,
 *      whereas a generic RuntimeException could mean anything.
 *   2. Targeted Handling — GlobalExceptionHandler has a dedicated @ExceptionHandler
 *      method for this exception, so it can return a proper HTTP 404 (Not Found) response
 *      with a meaningful error message, instead of a generic 500 (Internal Server Error).
 *   3. Extensibility — In the future, you can add more fields to this exception
 *      (e.g., the resource type, the ID that was searched for) for richer error responses.
 *
 * HOW IT CONNECTS:
 *   CourseServiceImpl throws this → GlobalExceptionHandler catches it
 *   → returns HTTP 404 JSON response to the frontend.
 *
 * EXAMPLE FLOW:
 *   GET /api/courses/999
 *   → CourseServiceImpl.getCourseById(999)
 *   → repository.findById(999) returns empty Optional
 *   → throws new ResourceNotFoundException("Course not found: 999")
 *   → GlobalExceptionHandler.handleResourceNotFound() catches it
 *   → responds with: { "status": 404, "message": "Course not found: 999", ... }
 */
public class ResourceNotFoundException extends RuntimeException {

    // We extend RuntimeException (unchecked) instead of Exception (checked) because:
    // - A "not found" scenario is a runtime business condition, not a recoverable error.
    // - Checked exceptions would force every method in the call chain to declare "throws",
    //   cluttering the code. Spring's @ExceptionHandler handles unchecked exceptions just fine.
    public ResourceNotFoundException(String message) {
        super(message); // Passes the error message up to RuntimeException's constructor.
    }
}