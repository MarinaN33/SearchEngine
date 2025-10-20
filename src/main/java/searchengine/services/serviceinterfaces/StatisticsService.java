package searchengine.services.serviceinterfaces;

import searchengine.dto.statistics.StatisticsResponse;

/**
 * Сервис для получения статистики по процессу индексации сайтов.
 */

public interface StatisticsService {

    /**
     * Возвращает текущую статистику индексации.
     *
     * @return объект {@link StatisticsResponse} с данными статистики
     */
    StatisticsResponse getStatistics();
}
