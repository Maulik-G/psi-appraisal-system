package com.maulik.appraisal.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public String test() {
        return "Only HR can access this!";
    }
}