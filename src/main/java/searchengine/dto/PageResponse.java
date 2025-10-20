package searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO, представляющее результат загрузки страницы.
 */

@Getter
@AllArgsConstructor
public class PageResponse {

    /**
     * HTTP-статус ответа.
     */
    private final int statusCode;

    /**
     * Содержимое страницы (HTML или текст).
     */
    private final String body;

    /**
     * Флаг, указывающий, является ли содержимое HTML.
     */
    private final boolean isHtml;
}
