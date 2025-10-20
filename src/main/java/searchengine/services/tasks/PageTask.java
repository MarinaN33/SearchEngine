package searchengine.services.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.dto.PageResponse;
import searchengine.logs.LogTag;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.util.IndexingContext;

import java.util.List;
import java.util.concurrent.RecursiveAction;

/**
 * Задача индексации одной страницы сайта.
 *
 * <p>Сохраняет страницу в БД, извлекает леммы и создаёт дочерние задачи для внутренних ссылок.
 */

@Slf4j
@RequiredArgsConstructor

public class PageTask extends RecursiveAction {

    private static final LogTag TAG = LogTag.PAGE_TASK;
    private final String url;
    private final String siteDomain;
    private final IndexingContext context;
    private final SiteEntity siteEntity;

    @Override
    protected void compute() {

        if (context.shouldStop("PageTask-" + url)) return;

        try {
            PageResponse resp = context.getManagerJSOUP().fetchPageWithContent(url);
            String htmlBody = (resp.getBody() != null) ? resp.getBody() : "<html><body></body></html>";

            PageEntity page = context.getEntityFactory().createPageEntity(
                    siteEntity,
                    url,
                    resp.getStatusCode(),
                    htmlBody
            );

            context.getDataManager().savePage(page);
            context.getLemmaFrequencyService().savePageLemmasAndIndexesThreadSafe(page, page.getContent());

            if (resp.isHtml() && resp.getBody() != null) {
                List<PageTask> refs = context.getManagerJSOUP()
                        .getLinksFromPage(url, siteDomain)
                        .stream()
                        .filter(context.getVisitedUrlStore()::visitUrl)
                        .map(link -> new PageTask(link, siteDomain, context, siteEntity))
                        .toList();
                if (!refs.isEmpty()) invokeAll(refs);
            }

        } catch (Exception e) {
            log.error("{}  Ошибка при обработке страницы {} возможно это изображение", TAG, url, e);
        }
    }
}
