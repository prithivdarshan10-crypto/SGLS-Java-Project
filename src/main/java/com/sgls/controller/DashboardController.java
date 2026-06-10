package com.sgls.controller;

import com.sgls.entity.User;
import com.sgls.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        long totalUsers = userRepository.count();

        long admins =
                userRepository.findByRole(User.Role.ADMIN).size();

        long managers =
                userRepository.findByRole(User.Role.MANAGER).size();

        long employees =
                userRepository.findByRole(User.Role.EMPLOYEE).size();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("admins", admins);
        model.addAttribute("managers", managers);
        model.addAttribute("employees", employees);

        return "dashboard";
    }
}
