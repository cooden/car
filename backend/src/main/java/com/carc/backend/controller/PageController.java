package com.carc.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "forward:/index.html"; // Ensure it forwards to the static index.html
    }

    @GetMapping("/model")
    public String model() {
        return "forward:/model.html"; // Forward to model.html
    }

    @GetMapping("/power")
    public String power() {
        return "forward:/power.html"; // Forward to power.html
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "forward:/privacy.html";
    }

    @GetMapping("/about")
    public String about() {
        return "forward:/about.html";
    }

    @GetMapping("/contact")
    public String contact() {
        return "forward:/contact.html";
    }
}