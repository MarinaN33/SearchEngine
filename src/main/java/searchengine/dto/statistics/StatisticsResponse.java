package searchengine.dto.statistics;

import lombok.Data;

/**
 * Ответ API со статистикой индексации.
 */

@Data
public class StatisticsResponse {

    /**
     * Результат выполнения запроса (true — успех).
     */
    private boolean result;

    /**
     * Статистические данные.
     */
    private StatisticsData statistics;
}
