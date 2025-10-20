package searchengine.dto.statistics;

import lombok.Data;

/**
 * Подробная статистика по каждому сайту.
 */

@Data
public class DetailedStatisticsItem {

    /**
     * URL сайта.
     */
    private String url;

    /**
     * Название сайта.
     */
    private String name;

    /**
     * Текущий статус (например: INDEXING, INDEXED, FAILED).
     */
    private String status;

    /**
     * Время последнего обновления статуса (timestamp, мс).
     */
    private long statusTime;

    /**
     * Сообщение об ошибке (если есть).
     */
    private String error;

    /**
     * Количество проиндексированных страниц.
     */
    private int pages;

    /**
     * Количество найденных лемм.
     */
    private int lemmas;
}
