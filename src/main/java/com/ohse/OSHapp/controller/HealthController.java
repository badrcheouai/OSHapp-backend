package com.ohse.OSHapp.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() { return "OK"; }

    @GetMapping("/admin/hello")
    public String admin() { return "hello admin"; }
}

