package com.aritra.d.riad.CoWork.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class BaseController {
    @GetMapping
    public String home() {
        return "Welcome to CoWork";
    }
}
