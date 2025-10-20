package searchengine.services.serviceinterfaces;

/**
 * Сервис для управления процессом индексации сайтов.
 * <p>Определяет методы для запуска и остановки индексации.</p>
 */

public interface IndexingService {

    /**
     * Запускает процесс индексации всех сайтов.
     */
    void startIndexing();

    /**
     * Останавливает текущий процесс индексации.
     */
    void stopIndexing();
}
