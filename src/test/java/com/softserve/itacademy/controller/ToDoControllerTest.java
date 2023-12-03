package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.*;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ToDoController.class)
public class ToDoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ToDoService toDoService;
    @MockBean
    private UserService userService;
    @MockBean
    private TaskService taskService;


    private final ToDo testTodo = new ToDo();
    private final User testUser = new User();

    @BeforeEach
    public void setUp() {
        Role testRole = new Role(2L, "USER");

        testUser.setId(1L);
        testUser.setEmail("test@gmail.com");
        testUser.setFirstName("First");
        testUser.setLastName("Last");
        testUser.setPassword("1234");
        testUser.setRole(testRole);

        testTodo.setId(1L);
        testTodo.setTitle("Todo");
        testTodo.setOwner(testUser);
        testTodo.setCreatedAt(LocalDateTime.now());
        testTodo.setTasks(new ArrayList<Task>());
        testTodo.setCollaborators(new ArrayList<User>());
    }

    @Test
    public void getCreateTodoTest() throws Exception {
        long ownerId = 1L;
        when(userService.readById(ownerId)).thenReturn(testUser);

        mockMvc.perform(get("/todos/create/users/{owner_id}", ownerId))
                .andExpect(model().attribute("todo", new ToDo()))
                .andExpect(model().attribute("ownerId", ownerId))
                .andExpect(status().isOk())
                .andExpect(view().name("create-todo"))
                .andDo(print());
    }

    @Test
    public void postCreateTodoTest() throws Exception {
        long ownerId = 1L;
        when(userService.readById(ownerId)).thenReturn(testUser);

        ToDo newToDo = new ToDo();
        BeanUtils.copyProperties(testTodo, newToDo);

        mockMvc.perform(post("/todos/create/users/{owner_id}", ownerId)
                        .param("id", String.valueOf(newToDo.getId()))
                        .param("title", newToDo.getTitle())
                        .param("createAt", String.valueOf(testTodo.getCreatedAt())))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todos/all/users/" + ownerId))
                .andDo(print());

        verify(toDoService).create(newToDo);
    }

    @Test
    public void getReadToDoTest() throws Exception {
        long todoId = 1L;
        long ownerId = 1L;
        when(toDoService.readById(todoId)).thenReturn(testTodo);
        when(userService.readById(ownerId)).thenReturn(testUser);

        mockMvc.perform(get("/todos/{id}/tasks", todoId,ownerId))
                .andExpect(model().attributeExists("todo"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("todo", testTodo))
                .andExpect(status().isOk())
                .andExpect(view().name("todo-tasks"))
                .andDo(print());
    }

    @Test
    public void getUpdateToDoTest() throws Exception {
        long todoId = 1L;
        long ownerId = 1L;
        when(toDoService.readById(todoId)).thenReturn(testTodo);
        when(userService.readById(ownerId)).thenReturn(testUser);

        mockMvc.perform(get("/todos/{todo_id}/update/users/{owner_id}", todoId,ownerId))
                .andExpect(model().attributeExists("todo"))
                .andExpect(model().attribute("todo", testTodo))
                .andExpect(status().isOk())
                .andExpect(view().name("update-todo"))
                .andDo(print());
    }

    @Test
    public void postUpdateToDoTest() throws Exception {
        long todoId = 1L;
        long userId = 1L;
        when(toDoService.readById(todoId)).thenReturn(testTodo);
        when(userService.readById(userId)).thenReturn(testUser);

        ToDo newToDo = new ToDo();
        BeanUtils.copyProperties(testTodo, newToDo);
        newToDo.setTitle("Updated Title");

        mockMvc.perform(post("/todos/{todo_id}/update/users/{owner_id}", todoId, userId)
                        .param("id", String.valueOf(newToDo.getId()))
                        .param("title", newToDo.getTitle()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todos/all/users/" + userId))
                .andDo(print());

        verify(toDoService).update(newToDo);
    }

    @Test
    public void getDeleteToDoTest() throws Exception {
        mockMvc.perform(get("/todos/{todo_id}/delete/users/{owner_id}", 1, 1))
                .andExpect(model().size(0))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todos/all/users/1"))
                .andDo(print());

        verify(toDoService).delete(1L);
    }

    @Test
    public void getAllToDoTest() throws Exception {
        long ownerId = 1L;
        when(userService.readById(ownerId)).thenReturn(testUser);
        when(toDoService.getByUserId(ownerId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/todos/all/users/{user_id}", ownerId))
                .andExpect(model().size(2))
                .andExpect(model().attribute("todos", new ArrayList<ToDo>()))
                .andExpect(model().attribute("user", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("todos-user"))
                .andDo(print());

        verify(toDoService).getByUserId(ownerId);
    }

    @Test
    public void addCollaboratorTest() throws Exception {
        Role testRole = new Role(2L, "USER");
        User newUser = new User();
        newUser.setId(2L);
        newUser.setEmail("test2@gmail.com");
        newUser.setFirstName("First");
        newUser.setLastName("Last");
        newUser.setPassword("1234");
        newUser.setRole(testRole);

        long todoId = 1L;
        long userId = 2L;
        when(toDoService.readById(todoId)).thenReturn(testTodo);
        when(userService.readById(userId)).thenReturn(newUser);

        mockMvc.perform(get("/todos/{id}/add", todoId)
                        .param("user_id", String.valueOf(userId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/" + todoId + "/tasks"))
                .andDo(print());
    }

    @Test
    public void removeCollaboratorTest() throws Exception {
        Role testRole = new Role(2L, "USER");
        User newUser = new User();
        newUser.setId(2L);
        newUser.setEmail("test2@gmail.com");
        newUser.setFirstName("First");
        newUser.setLastName("Last");
        newUser.setPassword("1234");
        newUser.setRole(testRole);

        long todoId = 1L;
        long userId = 2L;
        when(toDoService.readById(todoId)).thenReturn(testTodo);
        when(userService.readById(userId)).thenReturn(newUser);

        mockMvc.perform(get("/todos/{id}/add", todoId)
                        .param("user_id", String.valueOf(userId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos/" + todoId + "/tasks"))
                .andDo(print());
    }

}

