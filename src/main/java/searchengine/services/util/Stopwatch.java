package searchengine.services.util;

import lombok.Getter;

/**
 * Класс {@code Stopwatch} представляет собой лёгкий инструмент
 * для точного измерения времени выполнения операций.
 */

@Getter
public class Stopwatch {
    /**
     * Время старта в наносекундах.
     */
    private long startTime;

    /**
     * Измеренное время в наносекундах.
     */
    private long elapsedNanos;

    /**
     * Запускает измерение времени.
     */
    public void start() {
        startTime = System.nanoTime();
    }

    /**
     * Останавливает измерение и возвращает прошедшее время в секундах.
     *
     * @return прошедшее время в секундах
     */
    public double stop() {
        elapsedNanos = System.nanoTime() - startTime;
        return getSeconds();
    }

    /**
     * Возвращает измеренное время в секундах.
     */
    public double getSeconds() {
        return elapsedNanos / 1_000_000_000.0;
    }

    /**
     * Возвращает измеренное время в миллисекундах.
     */
    public double getMillis() {
        return elapsedNanos / 1_000_000.0;
    }

    /**
     * Сбрасывает измеренные данные, позволяя использовать таймер повторно.
     */
    public void reset() {
        startTime = 0L;
        elapsedNanos = 0L;
    }
}
