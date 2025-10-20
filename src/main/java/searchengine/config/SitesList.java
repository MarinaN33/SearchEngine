package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Конфигурационный бин, содержащий список сайтов для индексации.
 * <p>
 * Список загружается из application.yml / application.properties
 * с префиксом {@code indexing-settings}.
 */

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class SitesList {

    /**
     * Список сайтов для индексации.
     * Каждый сайт представлен объектом {@link Site}.
     */
    private List<Site> sites;
}
