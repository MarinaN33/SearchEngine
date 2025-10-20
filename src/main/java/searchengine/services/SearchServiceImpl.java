package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResult;
import searchengine.logs.LogTag;
import searchengine.model.Status;
import searchengine.services.serviceinterfaces.SearchService;
import searchengine.services.util.IndexingContext;
import searchengine.services.util.Stopwatch;
import java.util.List;

/*
 * Сервис поиска сайтов по запросу
 */

@Service
@RequiredArgsConstructor
@Slf4j

public class SearchServiceImpl implements SearchService {

    private static final LogTag TAG = LogTag.SEARCH_SERVER;

    /** Таймер для измерения времени поиска. */
    private Stopwatch stopwatch = new Stopwatch();

    /** Контекст индексации с доступом к данным и сервису лемм. */
    private final IndexingContext context;

    /**
     * Проверяет, готов ли индекс для поиска.
     *
     * <p>Если указан конкретный сайт, проверяется его статус индексации.
     * Если сайт не указан, проверяется, есть ли хотя бы один индексированный сайт.
     *
     * @param site URL сайта или null для проверки всех сайтов
     * @return true, если индекс готов для поиска, иначе false
     */
    public boolean isIndexReady(String site){
        if (site != null) {
            if (!isSiteIndexed(site)) return false;
        } else {
            if (!hasAnySites()) return false;
        }
        log.info("{}  сервис готов к поиску", TAG);
        return  context.getDataManager().hasLemmas();
    }

    /**
     * Выполняет поиск по запросу в индексе.
     *
     * @param query  поисковая строка
     * @param url    URL сайта для ограничения поиска (может быть null)
     * @param offset смещение для пагинации
     * @param limit  количество результатов
     * @return список результатов поиска {@link SearchResult}
     * @throws IllegalStateException если индекс ещё не готов
     */
    @Override
    public List<SearchResult> search(String query, String url, int offset, int limit) throws IllegalStateException{
        if (!isIndexReady(url)) {
            throw new IllegalStateException("Индекс ещё не готов. Попробуйте позже.");
        }
          log.info("{}  Начат поиск по запросу '{}' для сайта '{}'", TAG, query, url);
          stopwatch.start();
          List<SearchResult> searchResul = context.getLemmaFrequencyService().searchResult(query, url, offset, limit);
          stopwatch.stop();
          log.info("{}  Поиск завершился за {} сек.", TAG, stopwatch.getSeconds());
          stopwatch.reset();
          return searchResul;
    }

    /**
     * Проверяет, проиндексирован ли конкретный сайт.
     *
     * @param url URL сайта
     * @return true, если сайт проиндексирован, иначе false
     */

    private boolean isSiteIndexed(String url) {
        return context.getDataManager()
                .findSite(url)
                .map(site -> site.getStatus() == Status.INDEXED)
                .orElse(false);
    }

    /**
     * Проверяет, есть ли хотя бы один индексированный сайт.
     *
     * @return true, если есть сайты, иначе false
     */
    private boolean hasAnySites(){
        return context.getDataManager().hasSites();
    }
}
