package com.sgls.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/shipments")
    public String shipments() {
        return "shipments";
    }

    @GetMapping("/inventory")
    public String inventory() {
        return "inventory";
    }

    @GetMapping("/warehouses")
    public String warehouses() {
        return "warehouses";
    }

    @GetMapping("/employees")
    public String employees() {
        return "employees";
    }

    @GetMapping("/analytics")
    public String analytics() {
        return "analytics";
    }
}
