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
    private Site siteDto;
    private final IndexingContext context;

    @Override
    protected void compute() {

        if (siteDto == null || siteDto.getUrl() == null) return;

        if (context.shouldStop("SiteTask-" + siteDto.getUrl())) return;

         siteDto = context.getEntityFactory().createSiteEntity(siteDto.getName(), siteDto.getUrl());
         context.getDataManager().saveSite(siteDto);
         context.getVisitedUrlStore().activateSite(siteDto);

        try {
            log.info("{}  Обработка сайта: {}", TAG, siteDto.getUrl());

            List<String> pages = context.getManagerJSOUP().getLinksFromPage(siteDto.getUrl(), siteDto.getUrl());

            if (context.shouldStop("SiteTask-pages-" + siteDto.getUrl())) return;

            log.info("{}  Найдено {} внутренних ссылок на {}", TAG, pages.size(), siteDto.getUrl());

            List<PageTask> pageTasks = pages.stream()
                    .filter(context.getVisitedUrlStore()::visitUrl)
                    .map(url -> new PageTask(url, siteDto.getUrl(), context, siteDto))
                    .collect(Collectors.toList());

            if (!pageTasks.isEmpty()) {
                invokeAll(pageTasks);
            }

           boolean hasFailedPages = pageTasks.stream().anyMatch(PageTask::isCompletedAbnormally);

            if (hasFailedPages) {
                failSite("Одна или несколько страниц завершились с ошибкой");
           } else {
                siteDto.setStatus(Status.INDEXED);
                siteDto.setLastError(null);
                siteDto.setStatusTime(LocalDateTime.now());
                context.getDataManager().saveSite(siteDto);
                log.info("{}  идет подсчет веса лемм", TAG);
                context.getLemmaFrequencyService().recalculateRankForAllSites(siteDto);
                log.info("{}  подсчет веса лемм завершен", TAG);
            }

        }catch (Exception e) {
            log.error("{}  Ошибка при обработке сайта {}: {}", TAG, siteDto.getUrl(), e.getMessage(), e);
            failSite(e.getMessage());
        }
    }

    private void failSite(String message) {
        siteDto.setStatus(Status.FAILED);
        siteDto.setLastError(message);
        siteDto.setStatusTime(LocalDateTime.now());
        context.getDataManager().saveSite(siteDto);
    }

}
