package dev.sung.tokyo_life.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String home(Principal principal, Model model) {
        model.addAttribute("username", principal.getName());

        return "admin/home";
    }
}