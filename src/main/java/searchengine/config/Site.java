package searchengine.config;

import lombok.Data;

/**
 * Класс Site представляет информацию о сайте.
 * <p>
 * Содержит URL и название сайта.
 */

@Data
public class Site {

    /**
     * URL сайта, например: "https://example.com"
     */
    private String url;

    /**
     * Название сайта для отображения в интерфейсе или логах
     */
    private String name;
}
