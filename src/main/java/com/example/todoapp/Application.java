package com.example.todoapp;

import com.example.todoapp.business.service.TaskService;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Main class of the application. Managing routing and HTTP layer.
 */
public class Application {

    public static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        log.info("In-memory repository initialised");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        TaskService taskService = new TaskService();

        server.createContext("/tasks", taskService);

        server.setExecutor(null);
        server.start();
        log.info("HTTP server started on http://localhost:8080");
    }
}