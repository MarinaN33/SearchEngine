package searchengine.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.ApiResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResult;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.exceptions.BadRequestException;
import searchengine.exceptions.InternalServerErrorException;
import searchengine.exceptions.NotFoundException;
import searchengine.logs.LogTag;
import searchengine.services.IndexingServiceImpl;
import searchengine.services.PageIndexingServiceImpl;
import searchengine.services.SearchServiceImpl;
import searchengine.services.serviceinterfaces.StatisticsService;

import javax.validation.constraints.NotBlank;
import java.util.List;

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

    @Operation(summary = "Запуск индексации всех сайтов")
    @GetMapping("/startIndexing")
    public ApiResponse startIndexing() {
        log.info("{}  запущен метод: GET /startIndexing", TAG);
        if (indexingService.isIndexing()) {
            throw new BadRequestException("Индексация уже запущена");
        }
        indexingService.startIndexing();
        return new ApiResponse(true, null);
    }

    @Operation(summary = "Остановка индексации")
    @GetMapping("/stopIndexing")
    public ApiResponse stopIndexing() {
        log.info("{}  запущен метод: GET /stopIndexing", TAG);
        if (!indexingService.isIndexing()) {
            throw new BadRequestException("Индексация не запущена");
        }
        indexingService.stopIndexing();
        return new ApiResponse(true, null);
    }

    @Operation(summary = "Получение статистики по индексированию")
    @GetMapping("/statistics")
    public StatisticsResponse statistics() {
        log.info("{}  Вызов метода GET /statistics", TAG);
        return statisticsService.getStatistics();
    }

    @Operation(summary = "Индексация конкретной страницы")
    @PostMapping("/indexPage")
    public ApiResponse indexPage(@RequestParam("url") @NotBlank String url) {
        log.info("{}  запущен метод: POST /indexPage для {}", TAG, url);
        boolean indexed = pageIndexingService.indexPage(url);
        if (!indexed) {
            throw new BadRequestException("Данная страница находится за пределами сайтов, указанных в конфигурации");
        }
        return new ApiResponse(true, null);
    }

    @Operation(summary = "Поиск по сайту/запросу")
    @GetMapping("/search")
    public Object search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {

        log.info("{} /api/search: query={}, site={}, offset={}, limit={}", TAG, query, site, offset, limit);

        if (query == null || query.trim().isEmpty()) {
            throw new BadRequestException("Задан пустой поисковый запрос");
        }

        try {
            List<SearchResult> results = searchService.search(query, site, offset, limit);
            if (results.isEmpty()) {
                throw new NotFoundException("По запросу ничего не найдено");
            }
            return new SearchResponse(true, results.size(), results);
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            log.error("{}  Внутренняя ошибка поиска", TAG, e);
            throw new InternalServerErrorException("Внутренняя ошибка сервера");
        }
    }
}


