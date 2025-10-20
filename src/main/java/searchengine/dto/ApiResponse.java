package searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Базовый DTO-ответ API.
 * <p>
 * Используется для всех REST-ответов контроллеров.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ApiResponse {

    /**
     * Результат выполнения операции (true — успех, false — ошибка).
     */
    private boolean result;

    /**
     * Сообщение об ошибке (null, если операция успешна).
     */
    private String error;
}
