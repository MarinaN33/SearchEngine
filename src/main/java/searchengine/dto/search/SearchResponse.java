package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import searchengine.dto.ApiResponse;
import java.util.List;

/**
 * Расширяет {@link ApiResponse} и представляет результат поискового запроса.
 * <p>
 * Используется для передачи результатов поиска клиенту в формате JSON.
 * Поля:
 * <ul>
 *   <li>{@code count} — общее количество найденных результатов</li>
 *   <li>{@code data} — список объектов {@link SearchResult}</li>
 * </ul>
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)

public class SearchResponse extends ApiResponse {

    /**
     * Количество найденных результатов.
     */
    private int count;

    /**
     * Список найденных результатов.
     */
    private List<SearchResult> data;

    /**
     * Конструктор для успешного ответа.
     *
     * @param result результат выполнения (обычно true)
     * @param count  количество найденных результатов
     * @param data   список результатов
     */
    public SearchResponse(boolean result, int count, List<SearchResult> data) {
        super(result, null);
        this.count = count;
        this.data = data;
    }
}
