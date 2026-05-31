package com.example.todoapp.dto;

public record TaskRequestDto(
        String title,
        String description,
        Boolean done
) {}