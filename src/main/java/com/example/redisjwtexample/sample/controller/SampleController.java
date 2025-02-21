package com.example.redisjwtexample.sample.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/sample")
public class SampleController {

    @GetMapping
    public Map<String, String> sample() {
        return Map.of("message", "hello");
    }
}
