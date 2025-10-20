package searchengine.dto.statistics;

import lombok.Data;

import java.util.List;

/**
 * Основная структура данных для статистического ответа.
 */

@Data
public class StatisticsData {

    /**
     * Сводная статистика по всем сайтам.
     */
    private TotalStatistics total;

    /**
     * Подробные данные по каждому сайту.
     */
    private List<DetailedStatisticsItem> detailed;
}
