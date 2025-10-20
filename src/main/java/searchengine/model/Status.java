package searchengine.model;

/**
 * Статусы индексации сайта.
 * <ul>
 *     <li>{@code INDEXING} — сайт в процессе индексации</li>
 *     <li>{@code INDEXED} — сайт успешно проиндексирован</li>
 *     <li>{@code FAILED} — произошла ошибка при индексации</li>
 * </ul>
 */

public enum Status {
    INDEXING,
    INDEXED,
    FAILED
}
