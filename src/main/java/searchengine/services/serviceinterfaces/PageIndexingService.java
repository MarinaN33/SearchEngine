package searchengine.services.serviceinterfaces;

/**
 * Сервис для индексации отдельных страниц по URL.
 */

public interface PageIndexingService {

    /**
     * Индексирует страницу по указанному URL.
     * @param url URL страницы для индексации
     * @return true, если страница успешно проиндексирована, false в случае ошибки
     */
    boolean indexPage(String url);
}
