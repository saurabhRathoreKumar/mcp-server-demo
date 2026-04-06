package com.mcp.mcp_server.repository;

import com.mcp.mcp_server.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Find an employee by email address.
     *
     * @param email the email address
     * @return Optional containing the employee if found
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Find all employees in a specific department.
     *
     * @param department the department name
     * @return list of employees in that department
     */
    List<Employee> findByDepartment(String department);

    /**
     * Find all employees in a department (case-insensitive).
     *
     * @param department the department name
     * @return list of employees in that department
     */
    List<Employee> findByDepartmentIgnoreCase(String department);

    /**
     * Find all active employees.
     *
     * @return list of active employees
     */
    List<Employee> findByActiveTrue();

    /**
     * Find all inactive employees.
     *
     * @return list of inactive employees
     */
    List<Employee> findByActiveFalse();

    /**
     * Search employees by first name or last name containing the text (case-insensitive).
     *
     * @param firstName partial first name
     * @param lastName partial last name
     * @return list of matching employees
     */
    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    /**
     * Get all distinct departments.
     *
     * @return list of distinct department names
     */
    @Query("SELECT DISTINCT e.department FROM Employee e WHERE e.department IS NOT NULL")
    List<String> findAllDepartments();

    /**
     * Find employees with salary greater than or equal to the given amount.
     *
     * @param salary the minimum salary
     * @return list of employees with that salary or higher
     */
    List<Employee> findBySalaryGreaterThanEqual(BigDecimal salary);
}


