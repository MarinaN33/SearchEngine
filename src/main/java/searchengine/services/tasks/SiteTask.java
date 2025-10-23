package searchengine.services.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.logs.LogTag;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.services.util.IndexingContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

/**
 * Задача индексации одного сайта.
 *
 * <p>Сохраняет дефолтный вариант сайта, получает страницы, создаёт задачи {@link PageTask} и считает вес лемм.
 */

@Slf4j
@RequiredArgsConstructor

public class SiteTask extends RecursiveAction {

    private static final LogTag TAG = LogTag.SITE_TASK;
    private final searchengine.config.Site site;
    private Site site;
    private final IndexingContext context;

    @Override
    protected void compute() {

        if (site == null || site.getUrl() == null) return;

        if (context.shouldStop("SiteTask-" + site.getUrl())) return;

         site = context.getEntityFactory().createSiteEntity(site.getName(), site.getUrl());
         context.getDataManager().saveSite(site);
         context.getVisitedUrlStore().activateSite(site);

        try {
            log.info("{}  Обработка сайта: {}", TAG, site.getUrl());

            List<String> pages = context.getManagerJSOUP().getLinksFromPage(site.getUrl(), site.getUrl());

            if (context.shouldStop("SiteTask-pages-" + site.getUrl())) return;

            log.info("{}  Найдено {} внутренних ссылок на {}", TAG, pages.size(), site.getUrl());

            List<PageTask> pageTasks = pages.stream()
                    .filter(context.getVisitedUrlStore()::visitUrl)
                    .map(url -> new PageTask(url, site.getUrl(), context, site))
                    .collect(Collectors.toList());

            if (!pageTasks.isEmpty()) {
                invokeAll(pageTasks);
            }

           boolean hasFailedPages = pageTasks.stream().anyMatch(PageTask::isCompletedAbnormally);

            if (hasFailedPages) {
                failSite("Одна или несколько страниц завершились с ошибкой");
           } else {
                site.setStatus(Status.INDEXED);
                site.setLastError(null);
                site.setStatusTime(LocalDateTime.now());
                context.getDataManager().saveSite(site);
                log.info("{}  идет подсчет веса лемм", TAG);
                context.getLemmaFrequencyService().recalculateRankForAllSites(site);
                log.info("{}  подсчет веса лемм завершен", TAG);
            }

        }catch (Exception e) {
            log.error("{}  Ошибка при обработке сайта {}: {}", TAG, site.getUrl(), e.getMessage(), e);
            failSite(e.getMessage());
        }
    }

    private void failSite(String message) {
        site.setStatus(Status.FAILED);
        site.setLastError(message);
        site.setStatusTime(LocalDateTime.now());
        context.getDataManager().saveSite(site);
    }

}
