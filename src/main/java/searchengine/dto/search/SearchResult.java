package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO, представляющий один результат поиска.
 * <p>
 * Используется в составе {@link SearchResponse}.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchResult {

    /**
     * URL сайта, на котором найден результат.
     */
    private String site;

    /**
     * Название сайта (из конфигурации или мета-тегов).
     */
    private String siteName;

    /**
     * Относительный URI страницы (например, "/news/123").
     */
    private String uri;

    /**
     * Заголовок страницы (title HTML-документа).
     */
    private String title;

    /**
     * Краткий фрагмент текста, где встречается запрос (сниппет).
     */
    private String snippet;

    /**
     * Релевантность результата (используется для сортировки).
     */
    private float relevance;
}
