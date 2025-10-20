package searchengine.services;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.logs.LogTag;
import searchengine.services.serviceinterfaces.IndexingService;
import searchengine.services.util.Stopwatch;

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
    public void startIndexing(){
        setStatusIndexing(true);
        stopwatch.start();
        managerTasks.startIndexTask();
        stopwatch.stop();
        setStatusIndexing(false);
        log.info("{}  Индексация прошла за {} cek.", TAG, stopwatch.getSeconds());
        stopwatch.reset();
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
