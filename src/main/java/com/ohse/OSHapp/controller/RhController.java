package com.ohse.OSHapp.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rh")
@PreAuthorize("hasRole('RESP_RH')")
public class RhController {

    @GetMapping("/hello")
    public String helloRh() {
        return "Hello RESPONSABLE RH";
    }
}
