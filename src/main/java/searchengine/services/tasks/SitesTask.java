package searchengine.services.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.services.util.IndexingContext;
import java.util.List;
import java.util.concurrent.RecursiveAction;

/**
 * Корневая задача индексации всех сайтов.
 *
 * <p>Используется ForkJoinPool для параллельной обработки сайтов.
 * Удаляет старые сайты перед повторной индексацией и создаёт задачи {@link SiteTask} для каждого сайта.
 */

@Slf4j
@RequiredArgsConstructor
@Transactional

public class SitesTask extends RecursiveAction {

    private final IndexingContext context;

    @Override
    protected void compute() {

        if (context.getSites() == null || context.getSites().getSites().isEmpty()) return;

        if (context.shouldStop("SitesTask")) return;

        context.getSites().getSites().stream()
                .map(Site::getUrl)
                .forEach(url -> context.getDataManager().deleteSite(url));
        List<SiteTask> siteTasks = context.getSites().getSites().stream()
                .map(site -> new SiteTask(site, context))
                .toList();
        invokeAll(siteTasks);
    }
}
