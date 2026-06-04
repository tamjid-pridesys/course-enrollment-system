package com.tamjid.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tamjid.course.model.Course;

/**
 * Repository interface for accessing the TAMJID_COURSES table.
 *
 * HOW SPRING DATA JPA WORKS:
 * You only need to define this interface — Spring Data JPA automatically provides
 * a full implementation at runtime. You never write an implementation class for this.
 *
 * By extending JpaRepository<Course, Long>:
 *   - The first type parameter (Course) specifies the entity class.
 *   - The second type parameter (Long) specifies the type of the primary key.
 *
 * INHERITED METHODS (provided automatically by JpaRepository):
 *   - findAll()              → SELECT * FROM TAMJID_COURSES
 *   - findById(Long id)      → SELECT * FROM TAMJID_COURSES WHERE COURSE_NO = ?
 *   - save(Course entity)    → INSERT or UPDATE (depending on whether the PK exists)
 *   - deleteById(Long id)    → DELETE FROM TAMJID_COURSES WHERE COURSE_NO = ?
 *   - count()                → SELECT COUNT(*) FROM TAMJID_COURSES
 *   - existsById(Long id)    → SELECT COUNT(*) > 0 FROM TAMJID_COURSES WHERE COURSE_NO = ?
 *   - deleteAll()            → DELETE FROM TAMJID_COURSES
 *   ... and many more.
 *
 * CUSTOM QUERIES:
 * If you need a query that JpaRepository doesn't provide, you can add methods here like:
 *   List<Course> findByCourseNameContaining(String keyword);  // search by name
 *   List<Course> findByCourseFeeLessThan(Double maxFee);      // courses under a price
 * Spring Data JPA will automatically generate the SQL from the method name.
 *
 * HOW IT CONNECTS:
 *   CourseServiceImpl injects this repository and calls its methods to interact with the database.
 *   Spring creates a proxy implementation of this interface at startup and registers it as a bean.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

}