package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.TaskDto;
import com.softserve.itacademy.dto.TaskTransformer;
import com.softserve.itacademy.model.*;
import com.softserve.itacademy.service.StateService;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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

@WebMvcTest(TaskController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskControllerTest {

    @MockBean
    private TaskService taskService;

    @MockBean
    private StateService stateService;

    @MockBean
    private ToDoService toDoService;

    @Autowired
    private MockMvc mockMvc;

    private final ToDo testToDo = new ToDo();
    private final State testState = new State();
    private final Task testTask = new Task();

    @BeforeAll
    public void setUp() {
        testToDo.setTitle("Test ToDo");
        testToDo.setId(1L);
        testState.setName("Test state");
        testTask.setName("Task");
        testTask.setPriority(Priority.LOW);
        testTask.setTodo(testToDo);
        testTask.setState(testState);
    }

    @Test
    @DisplayName("When GET /tasks/create/todos/{todo_id} should return `create-task` view")
    public void getCreateTaskTest() throws Exception {
        long id = 1L;
        when(toDoService.readById(id)).thenReturn(testToDo);
        mockMvc.perform(get("/tasks/create/todos/{todo_id}", id))
                .andExpect(model().attribute("task", new TaskDto()))
                .andExpect(model().attribute("todo", testToDo))
                .andExpect(model().attribute("priorities", Priority.values()))
                .andExpect(status().isOk())
                .andExpect(view().name("create-task"))
                .andDo(print());
    }

    @Test
    @DisplayName("When POST /tasks/create/todos/{todo_id} should save Task to DB and redirect to correct view")
    public void postCreateTaskTest() throws Exception {
        long id = 1L;

        when(toDoService.readById(id)).thenReturn(testToDo);

        mockMvc.perform(post("/tasks/create/todos/{todo_id}", id)
                        .param("todoId", String.valueOf(id))
                        .param("priority", String.valueOf(Priority.LOW))
                        .param("name", testTask.getName()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todos/" + id + "/tasks"))
                .andDo(print());

        verify(taskService).create(any(Task.class));
    }

    @Test
    @DisplayName("When POST /tasks/create/todos/{todo_id} with invalid params should return same page with errors")
    public void postCreateTaskTestWithInvalidParams() throws Exception {
        long id = 1L;

        when(toDoService.readById(id)).thenReturn(testToDo);

        mockMvc.perform(post("/tasks/create/todos/{todo_id}", id)
                        .param("todoId", String.valueOf(id))
                        .param("priority", String.valueOf(Priority.LOW))
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("task", "name", "NotBlank"))
                .andExpect(model().attributeExists("todo", "priorities"))
                .andExpect(view().name("create-task"))
                .andDo(print());

        verify(taskService, never()).create(any(Task.class));
    }

    @Test
    @DisplayName("When POST /tasks/create/todos/{todo_id} with invalid todo_id should return error page")
    public void postCreateTaskTestWithInvalidToDoId() throws Exception {
        long invalidId = 2L;

        when(toDoService.readById(invalidId)).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(post("/tasks/create/todos/{todo_id}", invalidId)
                        .param("todoId", String.valueOf(invalidId))
                        .param("priority", String.valueOf(Priority.LOW))
                        .param("name", testTask.getName()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andDo(print());

        verify(taskService, never()).create(any(Task.class));
    }

    @Test
    @DisplayName("When GET /tasks/{task_id}/update/todos/{todo_id} should return `update-task` view")
    public void getUpdateTaskTest() throws Exception {
        long taskId = 1L;
        long todoId = 2L;

        when(taskService.readById(taskId)).thenReturn(testTask);

        mockMvc.perform(get("/tasks/{task_id}/update/todos/{todo_id}", taskId, todoId))
                .andExpect(model().attribute("task", TaskTransformer.convertToDto(testTask)))
                .andExpect(model().attribute("priorities", Priority.values()))
                .andExpect(model().attribute("states", new ArrayList<State>()))
                .andExpect(status().isOk())
                .andExpect(view().name("update-task"))
                .andDo(print());

        verify(taskService, never()).create(any(Task.class));
    }

    @Test
    @DisplayName("When POST /tasks/{task_id}/update/todos/{todo_id} should redirect to correct view")
    public void postUpdateTaskTest() throws Exception {
        long taskId = 1L;
        long todoId = 2L;

        when(taskService.readById(taskId)).thenReturn(testTask);

        mockMvc.perform(post("/tasks/{task_id}/update/todos/{todo_id}", taskId, todoId)
                        .param("id", String.valueOf(taskId))
                        .param("todoId", String.valueOf(todoId))
                        .param("name", testTask.getName())
                        .param("priority", String.valueOf(testTask.getPriority()))
                        .param("state", String.valueOf(testTask.getState())))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todos/" + todoId + "/tasks"))
                .andDo(print());

        verify(taskService).update(any(Task.class));
    }

    @Test
    @DisplayName("When POST /tasks/{task_id}/update/todos/{todo_id} with invalid params should return same page with errors")
    public void postUpdateTaskTestWithInvalidParams() throws Exception {
        long taskId = 1L;
        long todoId = 2L;

        when(taskService.readById(taskId)).thenReturn(testTask);

        mockMvc.perform(post("/tasks/{task_id}/update/todos/{todo_id}", taskId, todoId)
                        .param("id", String.valueOf(taskId))
                        .param("todoId", String.valueOf(todoId))
                        .param("name", "")
                        .param("priority", String.valueOf(testTask.getPriority()))
                        .param("state", String.valueOf(testTask.getState())))
                .andExpect(status().isOk())
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("task", "name", "NotBlank"))
                .andExpect(model().attributeExists("states", "priorities"))
                .andExpect(view().name("update-task"))
                .andDo(print());

        verify(taskService, never()).update(any(Task.class));
    }

    @Test
    @DisplayName("When POST /tasks/{task_id}/update/todos/{todo_id} with invalid params should return same page with errors")
    public void postUpdateTaskTestWithInvalidTaskId() throws Exception {
        long taskId = 1L;
        long invalidToDoId = 2L;

        when(taskService.readById(taskId)).thenReturn(testTask);
        when(toDoService.readById(invalidToDoId)).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(post("/tasks/{task_id}/update/todos/{todo_id}", taskId, invalidToDoId)
                        .param("id", String.valueOf(taskId))
                        .param("todoId", String.valueOf(invalidToDoId))
                        .param("name", testTask.getName())
                        .param("priority", String.valueOf(testTask.getPriority()))
                        .param("state", String.valueOf(testTask.getState())))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andDo(print());

        verify(taskService, never()).update(any(Task.class));
    }

    @Test
    @DisplayName("When GET /{task_id}/delete/todos/{todo_id} should remove task from DB and return correct view")
    public void getDeleteTaskTest() throws Exception {
        long taskId = 1L;
        long todoId = 2L;

        mockMvc.perform(get("/tasks/{task_id}/delete/todos/{todo_id}", taskId, todoId))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todos/" + todoId + "/tasks"))
                .andDo(print());

        verify(taskService).delete(taskId);
    }

    @Test
    @DisplayName("When GET /{task_id}/delete/todos/{todo_id} with invalid task_id return error page")
    public void getDeleteTaskTestWithInvalidTaskId() throws Exception {
        long invalidTaskId = 1L;
        long todoId = 2L;

        doThrow(EntityNotFoundException.class).when(taskService).delete(Long.parseLong(String.valueOf(invalidTaskId)));

        mockMvc.perform(get("/tasks/{task_id}/delete/todos/{todo_id}", invalidTaskId, todoId))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andDo(print());
    }
}