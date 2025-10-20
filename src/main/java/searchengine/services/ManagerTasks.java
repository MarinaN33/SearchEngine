package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.logs.LogTag;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.services.tasks.SitesTask;
import searchengine.services.util.IndexingContext;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
@Getter
@RequiredArgsConstructor

/**
 * Менеджер задач индексации
 * <p>Отвечает за запуск и остановку многопоточной индексации всех сайтов,
 * используя {@link ForkJoinPool} и корневую задачу {@link SitesTask}.
 * Все необходимые компоненты для работы индексации хранятся в {@link IndexingContext}.
 */

public class ManagerTasks {

    private static final LogTag TAG = LogTag.MANAGER_TASKS;

    /** Пул потоков для выполнения задач индексации. */
    private ForkJoinPool pool;

    /** Контекст индексации с необходимыми сервисами и данными. */
    private final IndexingContext context;


    /**
     * Запускает задачи индексации всех сайтов.
     */
    public void startIndexTask(){
        context.clearStopRequest();
        pool = new ForkJoinPool();
        context.getVisitedUrlStore().resetAll();

        log.info("{}  ИНДЕКСАЦИЯ УСПЕШНО ЗАПУЩЕНА...", TAG);
      try {

          pool.invoke(new SitesTask(context));
      }finally {
          log.info("{} ИНДЕКСАЦИЯ ЗАВЕРШИНА...", TAG);
      }
    }

    /**
     * Останавливает текущую индексацию.
     */
    public void stopIndexingTask() {
        log.info("{}  Останавливаем индексацию: ставим флаг и шлём shutdownNow() пулу", TAG);
        context.requestStop();
        pool.shutdown();
        try {
            if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        updateSitesAfterStop();
    }

    /**
     * Обновляет статус сайтов после принудительной остановки индексации.
     * <p>Все активные сайты получают статус FAILED с указанием ошибки.
     */
    private void updateSitesAfterStop() {
        Collection<SiteEntity> activeSites = context.getVisitedUrlStore().getActiveSites();
        for (SiteEntity site : activeSites) {
            site.setStatus(Status.FAILED);
            site.setLastError("Пользователь остановил индексацию");
            site.setStatusTime(LocalDateTime.now());
            context.getDataManager().saveSite(site);
        }
    }
}
