package com.softserve.itacademy.controller;

import com.softserve.itacademy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(HomeController.class);

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/", "home"})
    public String home(Model model) {
        logger.info("GET method 'home' of HomeController was called.");
        model.addAttribute("users", userService.getAll());
        return "home";
    }
}