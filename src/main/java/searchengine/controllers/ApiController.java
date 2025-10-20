package searchengine.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.ApiResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResult;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.logs.LogTag;
import searchengine.services.IndexingServiceImpl;
import searchengine.services.PageIndexingServiceImpl;
import searchengine.services.SearchServiceImpl;
import searchengine.services.serviceinterfaces.StatisticsService;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * Контроллер API для управления индексацией сайтов, поиском и статистикой.
 * <p>
 * Основные функции:
 * <ul>
 *     <li>Запуск и остановка индексации всех сайтов</li>
 *     <li>Индексация конкретной страницы по URL</li>
 *     <li>Получение статистики по индексированию</li>
 *     <li>Поиск по сайтам и запросам</li>
 * </ul>
 * <p>
 * Аннотация {@link Tag} используется для группировки методов в Swagger/OpenAPI UI.
 */

@Tag(name = "API", description = "Методы индексации, поиска и статистики")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {
    private static final LogTag TAG = LogTag.API;
    private final StatisticsService statisticsService;
    private final IndexingServiceImpl indexingService;
    private final SearchServiceImpl searchService;
    private final PageIndexingServiceImpl pageIndexingService;


    /**
     * Запускает индексацию всех сайтов, указанных в конфигурации.
     * <p>
     * Проверяет, не запущена ли уже индексация. Если индексация уже запущена,
     * возвращает {@link HttpStatus#BAD_REQUEST}.
     *
     * @return {@link ResponseEntity} с {@link ApiResponse} об успешности операции.
     */
    @Operation(summary = "Запуск индексации всех сайтов")
    @GetMapping("/startIndexing")
    public ResponseEntity<ApiResponse> startIndexing() {
        log.info("{}  запущен метод: GET /startIndexing", TAG);
        if (indexingService.isIndexing()) {
            log.warn("{}  Попытка запустить индексацию при уже запущенном процессе", TAG);
            return error("Индексация уже запущена", HttpStatus.BAD_REQUEST);
        }
        indexingService.startIndexing();
        return ResponseEntity.ok(new ApiResponse(true, null));
    }

    /**
     * Останавливает текущую индексацию всех сайтов.
     * <p>
     * Если индексация не запущена, возвращает {@link HttpStatus#BAD_REQUEST}.
     *
     * @return {@link ResponseEntity} с {@link ApiResponse} об успешности операции.
     */
    @Operation(summary = "Остановка индексации")
    @GetMapping("/stopIndexing")
    public ResponseEntity<ApiResponse> stopIndexing() {
        log.info("запущен метод: GET /stopIndexing");
        if (!indexingService.isIndexing()) {
            log.warn("{}  Попытка остановить индексацию, которая не запущена", TAG);
            return error("Индексация не запущена", HttpStatus.BAD_REQUEST);
        }
        log.warn("{}  попытка остановить индексацию", TAG);
            indexingService.stopIndexing();
            return ResponseEntity.ok(new ApiResponse(true, null));
    }

    /**
     * Возвращает статистику по индексированию сайтов.
     *
     * @return {@link ResponseEntity} с объектом {@link StatisticsResponse}.
     */
    @Operation(summary = "Получение статистики по индексированию")
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        log.info("{}  Вызов метода GET /statistics", TAG);
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    /**
     * Индексирует конкретную страницу по указанному URL.
     * <p>
     * Если страница находится за пределами сайтов, указанных в конфигурации,
     * возвращает {@link HttpStatus#BAD_REQUEST}.
     *
     * @param url URL страницы для индексации, не может быть пустым
     * @return {@link ResponseEntity} с {@link ApiResponse} об успешности операции.
     */
    @Operation(summary = "Индексация конкретной страницы")
    @PostMapping("/indexPage")
    public ResponseEntity<ApiResponse> indexPage(@RequestParam("url") @NotBlank String url) {
        log.info("{}  запущен метод: Post /indexPage", TAG);
        log.info("{}  Запуск индексации страницы {}", TAG, url);
        boolean indexed =  pageIndexingService.indexPage(url);
        if (!indexed) {
            log.warn("{}  Индексация страницы {} не выполнена", TAG, url);
            return error("Данная страница находится за пределами сайтов, указанных в конфигурационном файле",
                    HttpStatus.BAD_REQUEST);
        }
        log.info("{} Индексация страницы {} прошла успешно", TAG, url);
        return ResponseEntity.ok(new ApiResponse(true, null));
    }

    /**
     * Выполняет поиск по сайту или по всему индексу.
     * <p>
     * Если запрос пустой, возвращает {@link HttpStatus#BAD_REQUEST}.
     * Если ничего не найдено, возвращает {@link HttpStatus#NOT_FOUND}.
     *
     * @param query  поисковый запрос (не обязателен, но пустой не допускается)
     * @param site   сайт для поиска (не обязателен)
     * @param offset смещение результатов (по умолчанию 0)
     * @param limit  количество результатов (по умолчанию 20)
     * @return {@link ResponseEntity} с {@link SearchResponse} или {@link ApiResponse} с ошибкой.
     */
    @Operation(summary = "Поиск по сайту/запросу")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        log.info("{} Запущен метод /api/search с параметрами: query={}, site={}, offset={}, limit={}", TAG,
                query, site, offset, limit);
        if (query == null || query.trim().isEmpty()) {
            return error("Задан пустой поисковый запрос", HttpStatus.BAD_REQUEST);
        }
        try {
           List<SearchResult> results = searchService.search(query, site, offset, limit);
            if (results.isEmpty()) {
                return error("По запросу ничего не найдено", HttpStatus.NOT_FOUND);
            }
            log.info("{} Поиск завершён: найдено {} результатов", TAG, results.size());
            return ResponseEntity.ok(new SearchResponse(true, results.size(), results));
        }catch (IllegalStateException e) {
            log.warn("{}  Ошибка поиска: {}", TAG, e.getMessage());
            return error(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("{}  Внутренняя ошибка поиска", TAG, e);
            return error("Внутренняя ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Вспомогательный метод для формирования ответа с ошибкой.
     *
     * @param message сообщение об ошибке
     * @param status  HTTP статус
     * @return {@link ResponseEntity} с {@link ApiResponse}
     */
    private ResponseEntity<ApiResponse> error(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ApiResponse(false, message));
    }
}