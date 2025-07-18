package com.oshapp.backend.repository;

import com.oshapp.backend.model.Appointment;
import com.oshapp.backend.model.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    @Query("SELECT a FROM Appointment a WHERE a.employee.user.id = :userId")
    Page<Appointment> findByEmployeeUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT a FROM Appointment a WHERE a.employee.user.id = :userId")
    List<Appointment> findByEmployeeUserId(@Param("userId") Long userId);
    
    @Query("SELECT a FROM Appointment a WHERE a.employee.user.id = :userId AND a.status = :status")
    List<Appointment> findByEmployeeUserIdAndStatus(@Param("userId") Long userId, @Param("status") AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE a.employee.user.id = :userId AND a.appointmentDate >= :now ORDER BY a.appointmentDate ASC")
    List<Appointment> findUpcomingByEmployeeUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT a FROM Appointment a WHERE a.nurse.id = :userId OR a.doctor.id = :userId")
    Page<Appointment> findByMedicalStaff(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT a FROM Appointment a WHERE " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:employeeId IS NULL OR a.employee.id = :employeeId) AND " +
           "(:dateFrom IS NULL OR a.appointmentDate >= :dateFrom) AND " +
           "(:dateTo IS NULL OR a.appointmentDate <= :dateTo)")
    Page<Appointment> findWithFilters(
            @Param("status") AppointmentStatus status,
            @Param("employeeId") Long employeeId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);
} 