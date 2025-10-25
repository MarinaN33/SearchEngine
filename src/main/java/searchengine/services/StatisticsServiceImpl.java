package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.logs.LogTag;
import searchengine.model.Site;
import searchengine.services.serviceinterfaces.StatisticsService;
import searchengine.services.util.IndexingContext;
import searchengine.services.util.Stopwatch;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для формирования статистики по индексированию сайтов.
 *
 * <p>Возвращает как агрегированную информацию по всем сайтам, так и детальные данные по каждому.
 */

@Service
@RequiredArgsConstructor
@Slf4j

public class StatisticsServiceImpl implements StatisticsService {

    private static final LogTag TAG = LogTag.STATISTICS;
    private Stopwatch stopwatch = new Stopwatch();

    /**
     * Контекст индексации с доступом к менеджерам данных и состоянию задач.
     */
    private final IndexingContext context;

    /** Сервис индексации для проверки текущего состояния процесса. */
    private final IndexingServiceImpl indexingServiceImp;

    /**
     * Получает текущую статистику по индексированию.
     *
     * @return {@link StatisticsResponse} с общей и детальной информацией по сайтам
     */
    @Override
    public StatisticsResponse getStatistics() {
        List<Site> siteEntities = context.getDataManager().getAllSites();
        log.info("{}  Формирование статистики для {} сайтов", TAG, siteEntities.size());

        stopwatch.start();

        TotalStatistics total = calculateTotal(siteEntities);
        List<DetailedStatisticsItem> detailed = buildDetailedStatistics(siteEntities);

        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);

        StatisticsResponse response = new StatisticsResponse();
        response.setStatistics(data);
        response.setResult(true);
        stopwatch.stop();
        log.info("{}  Подсчет статистики завершился за {} сек.", TAG, stopwatch.getSeconds());
        stopwatch.reset();

        return response;
    }

    /**
     * Подсчитывает агрегированную статистику по всем сайтам.
     *
     * @param siteEntities список сайтов
     * @return {@link TotalStatistics} с суммарным количеством страниц, лемм и состоянием индексации
     */
    private TotalStatistics calculateTotal(List<Site> siteEntities) {
        TotalStatistics total = new TotalStatistics();
        total.setSites(siteEntities.size());
        total.setIndexing(indexingServiceImp.isIndexing());

        for (Site site : siteEntities) {
            int pages = site.getPageList().size();
            int lemmas = context.getDataManager().getCountLemmasBySite(site);

            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
        }

        return total;
    }

    /**
     * Формирует детальную статистику для каждого сайта.
     *
     * @param siteEntities список сайтов
     * @return список {@link DetailedStatisticsItem} с информацией по каждому сайту
     */
    private List<DetailedStatisticsItem> buildDetailedStatistics(List<Site> siteEntities) {
        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        for (Site site : siteEntities) {
            DetailedStatisticsItem item = mapSiteToStatisticsItem(site);
            detailed.add(item);
        }
        return detailed;
    }

    /**
     * Преобразует объект Site в DetailedStatisticsItem.
     *
     * @param site сайт
     * @return {@link DetailedStatisticsItem} с подробной информацией
     */
    private DetailedStatisticsItem mapSiteToStatisticsItem(Site site) {
        DetailedStatisticsItem item = new DetailedStatisticsItem();

        item.setName(site.getName());
        item.setUrl(site.getUrl());
        item.setStatus(site.getStatus().name());
        item.setError(site.getLastError());
        item.setStatusTime(site.getStatusTime()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());
        item.setPages(site.getPageList().size());
        item.setLemmas(context.getDataManager().getCountLemmasBySite(site));

        return item;
    }
}