package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.Role;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.RoleService;
import com.softserve.itacademy.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;


import javax.persistence.EntityNotFoundException;

import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private RoleService roleService;

    @Autowired
    private MockMvc mockMvc;

    private final User testUser1 = new User();
    private Role testRole1;

    @BeforeAll
    public void setUp() {
        testRole1 = new Role(2L, "USER");

        testUser1.setPassword("1234");
        testUser1.setId(1L);
        testUser1.setEmail("test@gmail.com");
        testUser1.setFirstName("First");
        testUser1.setLastName("Last");
        testUser1.setRole(testRole1);
    }

    @Test
    @DisplayName("When GET /users/create should return correct view with empty User")
    public void getCreateUserTest() throws Exception {
        mockMvc.perform(get("/users/create"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", new User()))
                .andExpect(status().isOk())
                .andExpect(view().name("create-user"))
                .andDo(print());
    }

    @Test
    @DisplayName("When POST /users/create should save User to DB and redirect to correct view")
    public void postCreateUserTest() throws Exception {
        User newUser = new User();
        BeanUtils.copyProperties(testUser1, newUser);
        newUser.setId(0);
        newUser.setRole(null);

        when(userService.create(newUser)).thenReturn(testUser1);

        mockMvc.perform(post("/users/create")
                        .param("firstName", newUser.getFirstName())
                        .param("lastName", newUser.getLastName())
                        .param("password", newUser.getPassword())
                        .param("email", newUser.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todos/all/users/" + testUser1.getId()))
                .andDo(print());

        verify(userService).create(newUser);
    }

    @Test
    @DisplayName("When POST /users/create with invalid params should return same page with errors")
    public void postCreateUserTestWithInvalidParams() throws Exception {
        User newUser = new User();
        BeanUtils.copyProperties(testUser1, newUser);
        newUser.setId(0);
        newUser.setEmail("invalid_email");
        when(userService.create(newUser)).thenReturn(testUser1);

        mockMvc.perform(post("/users/create")
                        .param("firstName", newUser.getFirstName())
                        .param("lastName", newUser.getLastName())
                        .param("password", newUser.getPassword())
                        .param("email", newUser.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("user", "email", "Pattern"))
                .andExpect(view().name("create-user"))
                .andDo(print());

        verify(userService, never()).create(newUser);
    }

    @Test
    @DisplayName("When GET /users/{id}/read should return user-info view with User with specified id")
    public void getReadUserTest() throws Exception {
        String userId = "1";
        when(userService.readById(Long.parseLong(userId))).thenReturn(testUser1);

        mockMvc.perform(get("/users/{id}/read", 1))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", testUser1))
                .andExpect(status().isOk())
                .andExpect(view().name("user-info"))
                .andDo(print());
    }

    @Test
    @DisplayName("When GET /users/{id}/read with invalid ID should return error view")
    public void getReadUserTestWithInvalidId() throws Exception {
        String invalidId = "2";
        when(userService.readById(Long.parseLong(invalidId))).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(get("/users/{id}/read", invalidId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andDo(print());
    }

    @Test
    @DisplayName("When GET /users/{id}/update should return update-user view with User with specified id")
    public void getUpdateUserTest() throws Exception {
        String userId = "1";
        when(userService.readById(Long.parseLong(userId))).thenReturn(testUser1);

        mockMvc.perform(get("/users/{id}/update", 1))
                .andExpect(model().attributeExists("user", "roles"))
                .andExpect(model().attribute("user", testUser1))
                .andExpect(status().isOk())
                .andExpect(view().name("update-user"))
                .andDo(print());
    }

    @Test
    @DisplayName("When POST /users/{id}/update should update User in DB and redirect to correct view")
    public void postUpdateUserTest() throws Exception {
        User newUser = new User();
        newUser.setEmail("newEmail@gmail.com");
        newUser.setPassword("newPass");
        newUser.setFirstName("Test");
        newUser.setLastName("Name");
        newUser.setRole(testRole1);
        newUser.setId(testUser1.getId());

        when(userService.readById(newUser.getId())).thenReturn(testUser1);
        when(roleService.readById(2)).thenReturn(testRole1);

        mockMvc.perform(post("/users/{id}/update", testUser1.getId())
                        .param("firstName", newUser.getFirstName())
                        .param("id", String.valueOf(newUser.getId()))
                        .param("roleId", String.valueOf(newUser.getRole().getId()))
                        .param("lastName", newUser.getLastName())
                        .param("password", newUser.getPassword())
                        .param("email", newUser.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/" + testUser1.getId() + "/read"))
                .andDo(print());

        verify(userService).update(newUser);
    }

    @Test
    @DisplayName("When POST /users/{id}/update with invalid params should return same page with errors")
    public void postUpdateUserTestWithInvalidParams() throws Exception {
        User newUser = new User();
        newUser.setEmail("invalid_email");
        newUser.setPassword("newPass");
        newUser.setFirstName("Test");
        newUser.setLastName("Name");
        newUser.setRole(testRole1);
        newUser.setId(testUser1.getId());

        when(userService.readById(newUser.getId())).thenReturn(testUser1);
        when(roleService.readById(2)).thenReturn(testRole1);

        mockMvc.perform(post("/users/{id}/update", testUser1.getId())
                        .param("firstName", newUser.getFirstName())
                        .param("id", String.valueOf(newUser.getId()))
                        .param("roleId", String.valueOf(newUser.getRole().getId()))
                        .param("lastName", newUser.getLastName())
                        .param("password", newUser.getPassword())
                        .param("email", "invalid_email"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("user", "email", "Pattern"))
                .andExpect(view().name("update-user"))
                .andDo(print());

        verify(userService, never()).update(newUser);
    }

    @Test
    @DisplayName("When POST /users/{id}/update with invalid Id should return error page")
    public void postUpdateUserTestWithInvalidId() throws Exception {
        String invalidId = "2";
        User newUser = new User();
        newUser.setEmail("email@gmail.com");
        newUser.setPassword("newPass");
        newUser.setFirstName("Test");
        newUser.setLastName("Name");
        newUser.setRole(testRole1);
        newUser.setId(testUser1.getId());

        when(userService.readById(Long.parseLong(invalidId))).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(post("/users/{id}/update", invalidId)
                        .param("firstName", newUser.getFirstName())
                        .param("id", String.valueOf(newUser.getId()))
                        .param("roleId", String.valueOf(newUser.getRole().getId()))
                        .param("lastName", newUser.getLastName())
                        .param("password", newUser.getPassword())
                        .param("email", newUser.getEmail()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andDo(print());

        verify(userService, never()).update(newUser);
    }


    @Test
    @DisplayName("When GET /users/{id}/update with invalid ID should return error view")
    public void getUpdateUserTestWithInvalidId() throws Exception {
        String invalidId = "2";
        when(userService.readById(Long.parseLong(invalidId))).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(get("/users/{id}/update", invalidId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andDo(print());
    }

    @Test
    @DisplayName("When GET /users/{id}/delete should remove User with specified id and redirect to users-list")
    public void getDeleteUserTest() throws Exception {
        mockMvc.perform(get("/users/{id}/delete", 1))
                .andExpect(model().size(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/all"))
                .andDo(print());

        verify(userService).delete(1L);
    }

    @Test
    @DisplayName("When GET /users/{id}/delete with invalid ID should return error view")
    public void getDeleteUserTestWithInvalidId() throws Exception {
        String invalidId = "2";
        doThrow(EntityNotFoundException.class).when(userService).delete(Long.parseLong(invalidId));

        mockMvc.perform(get("/users/{id}/delete", invalidId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andDo(print());
    }

    @Test
    @DisplayName("When GET /users/all should return users-user view List of all users")
    public void getAllUsersTest() throws Exception {
        when(userService.getAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/users/all"))
                .andExpect(model().size(1))
                .andExpect(model().attribute("users", new ArrayList<User>()))
                .andExpect(status().isOk())
                .andExpect(view().name("users-list"))
                .andDo(print());

        verify(userService).getAll();
    }

}