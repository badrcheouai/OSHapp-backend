package com.ohse.OSHapp.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/infirmier")
@PreAuthorize("hasRole('INFIRMIER_ST')")
public class InfirmierController {

    @GetMapping("/hello")
    public String helloInfirmier() {
        return "Hello INFIRMIER ST";
    }
}
