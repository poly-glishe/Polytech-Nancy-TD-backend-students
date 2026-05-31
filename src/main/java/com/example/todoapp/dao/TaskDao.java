package com.example.todoapp.dao;

import com.example.todoapp.business.model.Task;

import java.util.Optional;
import java.util.ArrayList;
import java.sql.*;

/**
 * Data Access Object for {@link Task} model.
 */

public class TaskDao {
    private final String url = "jdbc:sqlite:C:/Users/Aurelio/OneDrive/Documents/Polytech/3A/S6/ProgWeb/Polytech-Nancy-TD-backend-students/tasks.db";

    public TaskDao() throws Exception {
        // Au démarrage, on crée la table si elle n'existe pas
        try (Connection conn = DriverManager.getConnection(url)) {
            conn.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT, 
                    title TEXT NOT NULL, 
                    description TEXT, 
                    done INTEGER DEFAULT 0
                )
            """);
        }
    }
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public ArrayList<Task> getAllTasks() throws Exception {
        var allTasks = new ArrayList<Task>();
        try (Connection conn = connect();
             var rs = conn.createStatement().executeQuery("SELECT * FROM tasks")) {
            while (rs.next()) {
                allTasks.add(new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getInt("done") == 1));
            }
        }
        return allTasks;
    }

    public Optional<Task> findById(int id) throws Exception {
        try (Connection conn = connect();
             var p = conn.prepareStatement("SELECT * FROM tasks WHERE id = ?")) {
            p.setInt(1, id);
            var rs = p.executeQuery();
            if (rs.next()) {
                return Optional.of(new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getInt("done") == 1));
            }
        }
        return Optional.empty();
    }

    public Task save(Task t) throws Exception {
        try (Connection conn = connect();
             var p = conn.prepareStatement("INSERT INTO tasks(title, description, done) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, t.title());
            p.setString(2, t.description());
            p.setInt(3, t.done() ? 1 : 0);
            p.executeUpdate();

            var keys = p.getGeneratedKeys();
            if (keys.next()) {
                return new Task(keys.getInt(1), t.title(), t.description(), t.done());
            }
        }
        return t;
    }

    public void update(int id, Task t) throws Exception {
        try (Connection conn = connect();
             var p = conn.prepareStatement("UPDATE tasks SET title = ?, description = ?, done = ? WHERE id = ?")) {
            p.setString(1, t.title());
            p.setString(2, t.description());
            p.setInt(3, t.done() ? 1 : 0);
            p.setInt(4, id);
            p.executeUpdate();
        }
    }

    public Optional<Task> deleteById(int id) throws Exception {
        var task = findById(id);
        if (task.isPresent()) {
            try (Connection conn = connect();
                 var p = conn.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
                p.setInt(1, id);
                p.executeUpdate();
            }
        }
        return task;
    }
}