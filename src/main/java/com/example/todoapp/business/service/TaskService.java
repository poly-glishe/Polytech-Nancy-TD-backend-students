package com.example.todoapp.business.service;

import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.presentation.TasksController;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class TaskService implements HttpHandler {
        public static TaskDao nouveaudao;

    static {
        try {
            nouveaudao = new TaskDao();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // Le service passe le relais au contrôleur pour exécuter l'action (GET, POST, etc.)
            TasksController.handleTasks(exchange);
        } catch (Exception e) {
            com.example.todoapp.Application.log.error("Erreur Service", e);
            exchange.sendResponseHeaders(500, -1);
            exchange.getResponseBody().close();
        }
    }
}