package org.example.application.dto;

public record Progress(long processed, long total, String elapsed) {
}
//Класс был создан при начале проэкта,
//Он был спроэктирован для показа прогресса в секундах,
//Во время разработки было выведенно решение в красивой голубой полоск,
//Класс можно удалить.
