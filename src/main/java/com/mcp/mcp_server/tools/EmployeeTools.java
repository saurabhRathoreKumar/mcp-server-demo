package com.mcp.mcp_server.tools;

import com.mcp.mcp_server.entity.Employee;
import com.mcp.mcp_server.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP Tools for querying the employees table.
 *
 * Each method annotated with @Tool becomes a callable tool that
 * Claude can invoke when an MCP client connects to this server.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeTools {

    private final EmployeeRepository employeeRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 1 – list all employees
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = """
            Retrieves all employees from the database.
            Returns a list with each employee's id, full name, email,
            department, salary and hire date.
            """)
    public List<Map<String, Object>> getAllEmployees() {
        log.debug("MCP tool called: getAllEmployees");
        return employeeRepository.findAll()
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 2 – get employee by id
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = """
            Retrieves a single employee by their numeric ID.
            Returns the employee's details or an error message if not found.
            """)
    public Map<String, Object> getEmployeeById(
            @ToolParam(description = "The numeric ID of the employee to look up") Long id) {

        log.debug("MCP tool called: getEmployeeById({})", id);
        return employeeRepository.findById(id)
                .map(this::toMap)
                .orElse(Map.of("error", "Employee not found with id: " + id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 3 – get employees by department
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = """
            Retrieves all employees belonging to a specific department.
            The department name is case-insensitive (e.g. 'engineering' or 'Engineering').
            """)
    public List<Map<String, Object>> getEmployeesByDepartment(
            @ToolParam(description = "Department name, e.g. Engineering, Marketing, HR, Sales") String department) {

        log.debug("MCP tool called: getEmployeesByDepartment({})", department);
        return employeeRepository.findByDepartmentIgnoreCase(department)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 4 – search employees by name
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = """
            Searches employees by a partial first or last name (case-insensitive).
            Useful when you know only part of an employee's name.
            """)
    public List<Map<String, Object>> searchEmployeesByName(
            @ToolParam(description = "Partial name to search for, e.g. 'ali' matches 'Alice'") String name) {

        log.debug("MCP tool called: searchEmployeesByName({})", name);
        return employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 5 – list all departments
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = "Returns the distinct list of all departments that exist in the employees table.")
    public List<String> getAllDepartments() {
        log.debug("MCP tool called: getAllDepartments");
        return employeeRepository.findAllDepartments();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 6 – get high-earners
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = """
            Retrieves employees whose salary is greater than or equal to the given minimum.
            Results are sorted from highest to lowest salary.
            """)
    public List<Map<String, Object>> getEmployeesAboveSalary(
            @ToolParam(description = "Minimum salary threshold, e.g. 90000") Double minSalary) {

        log.debug("MCP tool called: getEmployeesAboveSalary({})", minSalary);
        return employeeRepository.findBySalaryGreaterThanEqual(new java.math.BigDecimal(minSalary))
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────
    private Map<String, Object> toMap(Employee e) {
        return Map.of(
                "id",         e.getId(),
                "firstName",  e.getFirstName(),
                "lastName",   e.getLastName(),
                "email",      e.getEmail(),
                "department", e.getDepartment() != null ? e.getDepartment() : "N/A",
                "salary",     e.getSalary() != null ? e.getSalary() : 0,
                "hireDate",   e.getHireDate() != null ? e.getHireDate().toString() : "N/A",
                "active",     e.getActive()
        );
    }
}

