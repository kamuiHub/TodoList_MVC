package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.RoleService;
import com.softserve.itacademy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/create")
    public String create(Model model) {
        logger.info("GET method 'create' of UserController was called.");

        model.addAttribute("user", new User());
        return "create-user";
    }

    @PostMapping("/create")
    public String create(@Validated @ModelAttribute("user") User user, BindingResult result) {
        logger.info("POST method 'create' of UserController was called.");

        if (result.hasErrors()) {
            for (FieldError error : result.getFieldErrors()) {
                logger.error("Validation error in field '{}': {} - Rejected value: '{}'",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue());
            }
            return "create-user";
        }

        user.setPassword(user.getPassword());
        user.setRole(roleService.readById(2));
        User newUser = userService.create(user);

        logger.info("User '{}' was created.", user);
        return "redirect:/todos/all/users/" + newUser.getId();
    }

    @GetMapping("/{id}/read")
    public String read(@PathVariable long id, Model model) {
        logger.info("GET method 'read' of UserController was called.");

        User user = userService.readById(id);
        model.addAttribute("user", user);
        return "user-info";
    }

    @GetMapping("/{id}/update")
    public String update(@PathVariable long id, Model model) {
        logger.info("GET method 'update' of UserController was called.");

        User user = userService.readById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getAll());
        return "update-user";
    }


    @PostMapping("/{id}/update")
    public String update(@PathVariable long id, Model model, @Validated @ModelAttribute("user") User user, BindingResult result, @RequestParam("roleId") long roleId) {
        logger.info("Post method 'update' of UserController was called.");

        User oldUser = userService.readById(id);
        if (result.hasErrors()) {
            for (FieldError error : result.getFieldErrors()) {
                logger.error("Validation error in field '{}': {} - Rejected value: '{}'",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue());
            }
            user.setRole(oldUser.getRole());
            model.addAttribute("roles", roleService.getAll());
            return "update-user";
        }
        if (oldUser.getRole().getName().equals("USER")) {
            user.setRole(oldUser.getRole());
        } else {
            user.setRole(roleService.readById(roleId));
        }
        userService.update(user);
        logger.info("User '{}' was updated.", user);

        return "redirect:/users/" + id + "/read";
    }


    @GetMapping("/{id}/delete")
    public String delete(@PathVariable("id") long id) {
        logger.info("GET method 'delete' of UserController was called.");

        userService.delete(id);
        logger.info("User with id '{}' was deleted.", id);

        return "redirect:/users/all";
    }

    @GetMapping("/all")
    public String getAll(Model model) {
        logger.info("GET method 'getAll' of UserController was called.");

        model.addAttribute("users", userService.getAll());
        return "users-list";
    }
}