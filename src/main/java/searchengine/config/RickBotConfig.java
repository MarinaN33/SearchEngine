package searchengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * Конфигурация для RickBot, загружается из application.yml / application.properties
 * с префиксом {@code rickbot}.
 * <p>
 * Содержит параметры для работы бота, включая user-agent, задержки между запросами и referer.
 */

@Configuration
@ConfigurationProperties(prefix = "rickbot")
@Data
public class RickBotConfig {

    /**
     * Список User-Agent строк, которые бот может использовать при запросах.
     * Позволяет имитировать разные браузеры для обхода ограничений.
     */
    private List<String> userAgents;

    /**
     * Минимальная задержка между запросами в миллисекундах.
     * Используется для предотвращения блокировки со стороны сайтов.
     */
    private int minDelayMs;

    /**
     * Максимальная задержка между запросами в миллисекундах.
     * Используется для случайной паузы между запросами.
     */
    private int maxDelayMs;

    /**
     * HTTP referer, который бот будет передавать при запросах.
     * Позволяет эмулировать переход с конкретной страницы.
     */
    private String referer;
}