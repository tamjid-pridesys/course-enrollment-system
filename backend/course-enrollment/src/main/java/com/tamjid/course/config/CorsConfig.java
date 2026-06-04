package com.tamjid.course.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures Cross-Origin Resource Sharing (CORS) for the application.
 *
 * WHY THIS IS NEEDED:
 * By default, browsers block web pages from making HTTP requests to a different
 * origin (different protocol, host, or port) than the one serving the page.
 * This is called the "Same-Origin Policy" and it's a browser security feature.
 *
 * In this project, the frontend (Angular) runs on http://localhost:4200,
 * while the backend (Spring Boot) runs on http://localhost:8080.
 * Because the ports differ, they are considered different origins.
 * Without CORS configuration, the Angular app would be blocked from
 * calling any of the REST APIs exposed by this backend.
 *
 * HOW IT CONNECTS:
 * - @Configuration tells Spring to pick up this class at startup.
 * - Implementing WebMvcConfigurer lets us customize Spring MVC settings
 *   without disabling Spring Boot's auto-configuration.
 * - This config applies globally to all /api/** endpoints, so both
 *   CourseController (/api/courses) and EnrollmentController (/api/enrollments)
 *   will accept requests from the Angular frontend.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow any URL path starting with /api/ to be accessed from the Angular frontend.
        // - allowedOrigins: Only http://localhost:4200 (Angular dev server) can call our APIs.
        // - allowedMethods: Only these HTTP methods are permitted from the frontend.
        //   GET    → fetch data (list courses, get single course)
        //   POST   → create new data (add a course, enroll)
        //   PUT    → update existing data (edit a course)
        //   DELETE → remove data (delete a course, cancel enrollment)
        //   OPTIONS → the "preflight" request browsers send before the actual request
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}