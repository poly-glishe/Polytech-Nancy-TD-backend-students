package com.example.todoapp.presentation;

import com.example.todoapp.business.model.Task;
import com.example.todoapp.business.service.TaskService;
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.JsonUtils;
import com.example.todoapp.dto.TaskRequestDto;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

public class TasksController {

    private static final TaskDao dao = TaskService.nouveaudao;
    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");

    public static void handleTasks(HttpExchange exchange) throws Exception {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            //region Manage POST /tasks
            if ("POST".equals(method) && "/tasks".equals(path)) {
                TaskRequestDto input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), TaskRequestDto.class);
                Task taskToSave = new Task(-1, input.title(), input.description(), input.done());
                Task createdTask = dao.save(taskToSave);

                exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
                sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
                return;
            }
            //endregion

            //region Manage GET /tasks/{id}
            Matcher m = ID_PATH.matcher(path);
            if ("GET".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                Optional<Task> task = dao.findById(id);

                if (task.isPresent()) {
                    sendResponse(exchange, 200, JsonUtils.serialize(task.get()));
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }
            if ("GET".equals(method) && "/tasks".equals(path)) {
                ArrayList<Task> alltask = dao.getAllTasks();

                if (!alltask.isEmpty()) {
                    sendResponse(exchange, 200, JsonUtils.serialize(alltask));
                } else {
                    sendResponse(exchange, 204, null);
                }
                return;
            }
            //endregion

            //region manage DELETE /tasks/{id}
            Matcher n = ID_PATH.matcher(path);
            if ("DELETE".equals(method) && n.matches()) {
                int id = Integer.parseInt(n.group(1));
                Optional<Task> task = dao.findById(id);

                if (task.isPresent()) {
                    dao.deleteById(id);
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }
            //endregion

            //region manage PUT /tasks/{id}
            Matcher p = ID_PATH.matcher(path);
            if ("PUT".equals(method) && p.matches()) {
                int id = Integer.parseInt(p.group(1));
                Optional<Task> task = dao.findById(id);

                if (task.isPresent()) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
                    TaskRequestDto input = JsonUtils.deserialize(body, TaskRequestDto.class);
                    Task taskToEdit = new Task(id, input.title(), input.description(), input.done());
                    dao.update(id, taskToEdit);
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }
            //endregion

            // Otherwise → 404
            sendResponse(exchange, 404, null);
        } catch(Exception e){
            // Catch the sqlite errors here because I can't be bothered
            com.example.todoapp.Application.log.error("Erreur sur l'API", e);

            // Otherwise 500
            sendResponse(exchange, 500, null);
        }
        }

        private static void sendResponse (HttpExchange exchange,int status, String json) throws IOException {
            if (nonNull(json)) {
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                byte[] bytes = json.getBytes(UTF_8);
                exchange.sendResponseHeaders(status, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                exchange.sendResponseHeaders(status, 0);
                exchange.close();
            }
        }

    }
