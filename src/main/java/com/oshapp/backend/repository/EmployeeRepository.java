package com.oshapp.backend.repository;

import com.oshapp.backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByUser_Id(Long userId);
    
    Optional<Employee> findByEmail(String email);
    
    List<Employee> findByDepartment(String department);
    
    List<Employee> findByPosition(String position);
    
    List<Employee> findByDepartmentAndPosition(String department, String position);
    
    boolean existsByEmail(String email);
    
    boolean existsByEmployeeId(String employeeId);
} 