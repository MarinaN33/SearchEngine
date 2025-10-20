package searchengine.services.serviceinterfaces;

import searchengine.dto.search.SearchResult;
import java.util.List;

/**
 * Сервис для выполнения поиска по сайтам и запросам.
 */

public interface SearchService {

   /**
    * Выполняет поиск по указанному запросу и сайту.
    *
    * @param query  поисковый запрос
    * @param site   URL сайта для ограничения поиска (может быть null)
    * @param offset смещение результатов (для постраничного вывода)
    * @param limit  максимальное количество результатов
    * @return список объектов {@link SearchResult} с найденными результатами
    * @throws IllegalStateException если поиск не может быть выполнен (например, индексация не завершена)
    */
   List<SearchResult> search (String query, String site, int offset, int limit) throws IllegalStateException;
}
