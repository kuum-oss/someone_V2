package org.example.application.dto;

public record Progress(long processed, long total, String elapsed) {
}
