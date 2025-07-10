package com.ohse.OSHapp.controller;

import com.ohse.OSHapp.model.Incident;
import com.ohse.OSHapp.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/incidents")
public class IncidentController {
    @Autowired
    private IncidentRepository repo;

    @GetMapping
    public List<Incident> all() { return repo.findAll(); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESP_HSE')")
    public Incident create(@RequestBody Incident incident) {
        return repo.save(incident);
    }
} 