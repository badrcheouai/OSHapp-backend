package com.ohse.OSHapp.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medecin")
@PreAuthorize("hasRole('MEDECIN_TRAVAIL')")
public class MedecinController {

    @GetMapping("/hello")
    public String helloMedecin() {
        return "Hello MEDECIN DU TRAVAIL";
    }
}
