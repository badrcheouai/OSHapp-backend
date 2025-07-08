package com.ohse.OSHapp.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hse")
@PreAuthorize("hasRole('RESP_HSE')")
public class HseController {

    @GetMapping("/hello")
    public String helloHse() {
        return "Hello RESPONSABLE HSE";
    }
}
