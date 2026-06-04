package com.tamjid.course;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point of the Course Enrollment System.
 *
 * @SpringBootApplication is a convenience annotation that combines three things:
 *   1. @Configuration       — marks this class as a source of bean definitions for Spring.
 *   2. @EnableAutoConfiguration — tells Spring Boot to automatically configure beans
 *                                 based on the dependencies present on the classpath
 *                                 (e.g., it detects spring-boot-starter-web and sets up
 *                                  an embedded Tomcat server, a DispatcherServlet, etc.).
 *   3. @ComponentScan       — tells Spring to scan the com.tamjid.course package (and
 *                             all sub-packages) for @Component, @Service, @Controller,
 *                             @Repository, etc., so they get registered automatically.
 *
 * When this class runs, Spring Boot:
 *   - Starts an embedded web server (Tomcat by default).
 *   - Connects to the database configured in application.properties.
 *   - Registers all REST controllers, services, and repositories found in this project.
 *   - Makes the API available at http://localhost:8080.
 */
@SpringBootApplication
public class CourseEnrollmentApplication {

    public static void main(String[] args) {
        // SpringApplication.run() bootstraps the entire application:
        // it creates the Spring Application Context (the IoC container),
        // wires all beans together, and starts the web server.
        SpringApplication.run(CourseEnrollmentApplication.class, args);
    }
}