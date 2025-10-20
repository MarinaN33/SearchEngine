package searchengine.services.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import searchengine.config.SitesList;
import searchengine.logs.LogTag;
import searchengine.services.LemmaFrequencyService;
import searchengine.services.LemmaProcessor;
import searchengine.services.DataManager;

/**
 * Контекст для процессов индексирования страниц.
 * <p>Содержит все необходимые компоненты для обхода сайтов, обработки страниц и лемм.
 * Предоставляет функциональность для управления состоянием процесса (остановка/проверка остановки).
 *
 * <p>Используемые компоненты:
 * <ul>
 *   <li>{@link SitesList} — список сайтов для индексирования</li>
 *   <li>{@link EntityFactory} — создание сущностей для БД</li>
 *   <li>{@link DataManager} — работа с БД</li>
 *   <li>{@link ManagerJSOUP} — извлечение ссылок и текста со страниц</li>
 *   <li>{@link LemmaProcessor} — обработка текста и генерация лемм</li>
 *   <li>{@link VisitedUrlStore} — хранение посещённых URL</li>
 *   <li>{@link LemmaFrequencyService} — работа с частотой лемм и индексами</li>
 * </ul>
 */

@Component
@RequiredArgsConstructor
@Getter
@Setter
@Slf4j

public class IndexingContext {

    private static final LogTag TAG = LogTag.INDEXING_CONTEXT;

    /** Список сайтов для обхода */
    private final SitesList sites;

    /** Фабрика сущностей для сохранения в БД */
    private final EntityFactory entityFactory;

    /** Менеджер работы с базой данных */
    private final DataManager dataManager;

    /** Компонент для получения страниц и извлечения текста */
    private final ManagerJSOUP managerJSOUP;

    /** Процессор лемм для текстов страниц */
    private final LemmaProcessor lemmaProcessor;

    /** Хранилище посещённых URL */
    private final VisitedUrlStore visitedUrlStore;

    /** Сервис работы с леммами и индексами (отложенная инициализация) */
    private final @Lazy LemmaFrequencyService lemmaFrequencyService;

    /** Флаг запроса остановки процесса индексирования */
    private volatile boolean stopRequested = false;

    /**
     * Запрашивает остановку текущего процесса индексирования.
     * <p>Устанавливает {@link #stopRequested} в true.
     */
    public void requestStop() {
        this.stopRequested = true;
    }


    /**
     * Сбрасывает запрос на остановку.
     * <p>Используется для возобновления процесса после остановки.
     */
    public void clearStopRequest() {
        this.stopRequested = false;
    }


    /**
     * Проверяет, должен ли процесс остановиться.
     * <p>Возвращает true, если:
     * <ul>
     *   <li>Был установлен {@link #stopRequested}</li>
     *   <li>Текущий поток был прерван</li>
     * </ul>
     * <p>Если остановка требуется, логирует информацию о прерывании задачи.
     *
     * @param taskName имя текущей задачи (для логирования)
     * @return true, если выполнение должно быть прервано, иначе false
     */
    public boolean shouldStop(String taskName) {
        boolean stop = stopRequested || Thread.currentThread().isInterrupted();
        if (stop) {
            log.info("{}  Остановка задачи {}: выполнение прервано", TAG, taskName);
        }
        return stop;
    }
}
