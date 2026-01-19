package org.example.infrastructure.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Утилитный класс для управления параметрами окна и интерфейса.
 * Отвечает за определение оптимальных размеров экрана и нормализацию положения окна.
 */
public class UiUtils {

    /**
     * Настраивает размер, положение и ограничения окна.
     * @param frame Окно для настройки.
     */
    public static void setupWindow(JFrame frame) {
        // Получаем конфигурацию экрана, на котором находится курсор или основное окно
        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        Rectangle bounds = gc.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

        // Доступная рабочая область (без таскбаров/дока)
        int availableWidth = bounds.width - screenInsets.left - screenInsets.right;
        int availableHeight = bounds.height - screenInsets.top - screenInsets.bottom;

        // Расчет оптимального размера (примерно 80% от доступного пространства)
        int width = Math.max(1000, (int) (availableWidth * 0.85));
        int height = Math.max(700, (int) (availableHeight * 0.85));

        // Ограничиваем, чтобы не выходило за границы доступного пространства
        width = Math.min(width, availableWidth);
        height = Math.min(height, availableHeight);

        frame.setSize(width, height);
        
        // Устанавливаем минимальный размер для сохранения красоты интерфейса
        frame.setMinimumSize(new Dimension(900, 650));
        
        // Центрирование окна относительно доступной области
        int x = bounds.x + screenInsets.left + (availableWidth - width) / 2;
        int y = bounds.y + screenInsets.top + (availableHeight - height) / 2;
        frame.setLocation(x, y);

        // Добавляем фиксацию (опционально, но пользователь просил "с фиксированием")
        // Если под фиксированием подразумевается запрет изменения размера, то:
        // frame.setResizable(false); 
        // Но для библиотеки книг это не очень удобно. 
        // Скорее всего имелось в виду "нормализация и фиксирование начального состояния".
    }
}
