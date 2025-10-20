package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.PageResponse;
import searchengine.logs.LogTag;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.serviceinterfaces.PageIndexingService;
import searchengine.services.util.IndexingContext;
import searchengine.services.util.Stopwatch;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/*
 * Сервис индексации одной страницы
 */

@Service
@Slf4j
@Setter
@Getter
@RequiredArgsConstructor

public class PageIndexingServiceImpl implements PageIndexingService {

    private static final LogTag TAG = LogTag.PAGE_INDEXING_SERVER;

    /** Контекст индексации с доступом к менеджерам данных и лемм. */
    private final IndexingContext context;


    /**
     * Индексирует страницу по указанному URL.
     *
     * <p>Метод выполняет следующие шаги:
     * 1. Скачивает контент страницы.
     * 2. Определяет, к какому сайту принадлежит URL.
     * 3. Если страница уже существует в БД — удаляет старые леммы и страницу.
     * 4. Сохраняет страницу и пересчитывает леммы.
     *
     * @param url URL страницы
     * @return true, если страница успешно проиндексирована, false при ошибках
     */
    public boolean indexPage(String url) {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();

        PageResponse response = context.getManagerJSOUP().fetchPageWithContent(url);

        if (response.getBody() == null) {
            log.warn("{}  Не удалось скачать контент для URL {}, код {}", TAG,  url, response.getStatusCode());
            return false;
        }

        SiteEntity siteEntity = findSiteForUrl(url);

        if (siteEntity == null) {
            log.warn("{}  Страница {} не принадлежит ни одному сайту из БД", TAG, url);
            return false;
        }

        context.getDataManager()
                .findPathPage(url)
                .ifPresent(existingPage -> {
                    context.getLemmaFrequencyService().decreaseLemmaFrequencies(existingPage);
                    context.getDataManager().deletePage(existingPage);
                    log.info("{}  Старая страница {} удалена перед обновлением", TAG, url);
                });

        savePageAndLemmas(siteEntity, url, response);
        stopwatch.stop();
        log.info("{}  Страница {} успешно сохранена для сайта {}. Время индексации: {} сек.",
                TAG, url, siteEntity.getUrl(), stopwatch.getSeconds());
        stopwatch.reset();
        return true;
    }

    /**
     * Находит сайт в БД, которому принадлежит указанный URL.
     *
     * @param url URL страницы
     * @return {@link SiteEntity} сайта или null, если сайт не найден
     */
    private SiteEntity findSiteForUrl(String url) {
        List<SiteEntity> sites = context.getDataManager().getAllSites();

        if (sites.isEmpty()) {
            log.warn("{}  База сайтов пуста! Добавьте хотя бы один сайт.", TAG);
            return null;
        }

        try {
            URL inputURL = new URL(url);
            for (SiteEntity site : sites) {
                try {
                    URL siteURL = new URL(site.getUrl());
                    if (matchesHostAndProtocol(inputURL, siteURL)) {
                        return site;
                    }
                } catch (MalformedURLException e) {
                    log.error("{}  Неверный URL в базе сайтов: {}", TAG, site.getUrl());
                }
            }
        } catch (MalformedURLException e) {
            log.error("{}  Неверный или недопустимый URL: {}", TAG, url);
        }
        return null;
    }

    /**
     * Проверяет, совпадают ли хост и протокол входного URL и сайта.
     *
     * @param input входной URL
     * @param site  URL сайта
     * @return true, если хост и протокол совпадают
     */

    private boolean matchesHostAndProtocol(URL input, URL site) {
        String inputHost = input.getHost().replaceFirst("^www\\.", "").toLowerCase();
        String siteHost = site.getHost().replaceFirst("^www\\.", "").toLowerCase();
        return inputHost.equals(siteHost) && input.getProtocol().equals(site.getProtocol());
    }

    /**
     * Сохраняет страницу и пересчитывает леммы.
     *
     * @param site     сайт, к которому принадлежит страница
     * @param path     URL страницы
     * @param response ответ с содержимым страницы
     */
    private void savePageAndLemmas(SiteEntity site, String path, PageResponse response) {
        PageEntity page = context.getEntityFactory().createPageEntity(
                site,
                path,
                response.getStatusCode(),
                response.getBody()
        );
        context.getDataManager().savePage(page);
        context.getLemmaFrequencyService().savePageLemmasAndIndexes(page, response.getBody());
    }
}
