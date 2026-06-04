package com.tamjid.course.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handler for the entire application.
 *
 * WHAT DOES THIS CLASS DO?
 * Instead of each controller method having its own try-catch blocks, this class
 * catches ALL exceptions thrown by ANY controller and converts them into clean,
 * structured JSON error responses. This is the "Global Exception Handler" pattern.
 *
 * HOW IT WORKS:
 *   1. A controller method throws an exception (e.g., ResourceNotFoundException).
 *   2. Spring looks for a matching @ExceptionHandler method in a @RestControllerAdvice class.
 *   3. The matching handler method builds a JSON error response and returns it with the
 *      appropriate HTTP status code.
 *
 * WHY IS THIS IMPORTANT?
 * Without this, Spring would return an ugly default error page (HTML with a stack trace)
 * which is:
 *   - Not useful for the Angular frontend (it expects JSON).
 *   - A security risk (stack traces reveal internal implementation details).
 *   - Inconsistent (different errors would look different).
 *
 * HOW IT CONNECTS:
 *   - CourseServiceImpl throws ResourceNotFoundException
 *   - handleResourceNotFound() catches it → returns 404 JSON
 *   - Any other unexpected exception → handleGenericException() catches it → returns 500 JSON
 */
@RestControllerAdvice // @RestControllerAdvice = @ControllerAdvice + @ResponseBody
// @ControllerAdvice tells Spring: "This class handles exceptions from ALL controllers."
// @ResponseBody ensures the return values are serialized to JSON (not rendered as views).
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException specifically.
     *
     * When CourseServiceImpl throws new ResourceNotFoundException("Course not found: 5"),
     * this method is invoked and returns an HTTP 404 response with this JSON body:
     * {
     *     "timestamp": "2026-06-04T12:00:00",
     *     "status": 404,
     *     "error": "Not Found",
     *     "message": "Course not found: 5"
     * }
     *
     * @ExceptionHandler(ResourceNotFoundException.class) tells Spring:
     *   "Whenever a ResourceNotFoundException is thrown, call THIS method."
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        // Build a structured error response body instead of returning a raw string.
        // Using a Map keeps it simple — in larger projects, you'd create an ErrorResponse DTO class.
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());   // When the error occurred
        body.put("status", HttpStatus.NOT_FOUND.value()); // HTTP 404
        body.put("error", "Not Found");               // Human-readable error category
        body.put("message", ex.getMessage());         // Specific details (e.g., "Course not found: 5")

        // ResponseEntity lets us control both the response body AND the HTTP status code.
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Catch-all handler for any exception that isn't caught by a more specific handler.
     *
     * This is a safety net. For example, if a database connection fails or a
     * NullPointerException occurs, this handler ensures the frontend still gets
     * a structured JSON error response instead of a raw stack trace.
     *
     * Returns HTTP 500 (Internal Server Error) with this JSON body:
     * {
     *     "timestamp": "2026-06-04T12:00:00",
     *     "status": 500,
     *     "error": "Internal Server Error",
     *     "message": "could not execute statement"
     * }
     *
     * NOTE: Spring matches @ExceptionHandler methods from most specific to least specific.
     * ResourceNotFoundException → handleResourceNotFound() (more specific, matched first)
     * Exception (everything else) → handleGenericException() (least specific, fallback)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value()); // HTTP 500
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}