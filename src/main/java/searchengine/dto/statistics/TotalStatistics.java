package searchengine.dto.statistics;

import lombok.Data;

/**
 * Сводная статистика по всем сайтам.
 */

@Data
public class TotalStatistics {

    /**
     * Общее количество сайтов, добавленных в систему.
     */
    private int sites;

    /**
     * Общее количество проиндексированных страниц.
     */
    private int pages;

    /**
     * Общее количество лемм (уникальных словоформ) в индексе.
     */
    private int lemmas;

    /**
     * Флаг, показывающий, выполняется ли сейчас индексация.
     */
    private boolean indexing;
}
