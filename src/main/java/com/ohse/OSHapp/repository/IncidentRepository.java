package com.ohse.OSHapp.repository;

import com.ohse.OSHapp.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {} 