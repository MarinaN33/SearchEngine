package searchengine.services;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.logs.LogTag;
import searchengine.services.serviceinterfaces.IndexingService;
import searchengine.services.util.Stopwatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Реализация сервиса индексации сайтов.
 *
 * <p>Отвечает за запуск и остановку процесса индексации.
 * Использует {@link ManagerTasks} для управления задачами обхода и обработки страниц.
 * {@link Stopwatch} применяется для измерения времени выполнения полной индексации.
 */

@Slf4j
@Setter
@Service
@RequiredArgsConstructor

public class IndexingServiceImpl implements IndexingService {

    private static final LogTag TAG = LogTag.INDEXING_SERVER;

    /** Флаг состояния индексации (true — индексация идет). */
    private volatile boolean statusIndexing = false;

    /** Менеджер задач для запуска и остановки процессов индексации. */
    private final ManagerTasks managerTasks;

    /** Таймер для измерения времени индексации. */
    private Stopwatch stopwatch = new Stopwatch();

    /** Пул потоков для асинхронного запуска задач */
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Проверяет, выполняется ли в данный момент индексация.
     *
     * @return true, если индексация активна, иначе false
     */
    public boolean isIndexing(){
        return statusIndexing;
    }


    /**
     * Запускает процесс индексации всех сайтов.
     */
   @Override
public void startIndexing() {
    if (isIndexing()) {
        log.warn("{} Индексация уже запущена", TAG);
        return;
    }
    if (executor.isShutdown() || executor.isTerminated()) {
        executor = Executors.newSingleThreadExecutor();
    }

    setStatusIndexing(true);
    executor.submit(() -> {
        try {
            stopwatch.start();
            managerTasks.startIndexTask();
        } catch (Exception e) {
            log.error("{} Ошибка при индексации", TAG, e);
        } finally {
            stopwatch.stop();
            stopwatch.reset();
            setStatusIndexing(false);
        }
    });
}

    /**
     * Останавливает текущую индексацию.
     */@Override
    public void stopIndexing() {
        try {
            managerTasks.stopIndexingTask();
        } finally {
            setStatusIndexing(false);
            stopwatch.stop();
            stopwatch.reset();
        }
    }
}
