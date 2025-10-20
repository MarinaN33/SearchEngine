package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.logs.LogTag;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Менеджер работы с данными сущностей: Sites, Pages, Lemmas, Indexes.
 * <p>
 * Содержит методы поиска, сохранения и удаления сущностей, а также проверки наличия данных.
 * Логирование ошибок и операций осуществляется через {@link LogTag#DATA_MANAGER}.
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class DataManager {

    private static final LogTag TAG = LogTag.DATA_MANAGER;
    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

   /* Унифицированный обработчик ошибок */
    private <T> T wrapOperation(Supplier<T> operation, String errorMsg, T defaultValue) {
        try {
            return operation.get();
        } catch (Exception e) {
            log.error("{} {}", TAG, errorMsg, e);
            return defaultValue;
        }
    }

    // ==== SITE METHODS ====

    /**
     * Получить все сайты.
     *
     * @return список всех SiteEntity
     */
    @Transactional(readOnly = true)
    public List<SiteEntity> getAllSites() {
        return wrapOperation(siteRepository::findAll, "Ошибка при получении всех сайтов", Collections.emptyList());
    }

    /**
     * Проверка, есть ли сайты в базе.
     *
     * @return true, если есть хотя бы один сайт
     */
    @Transactional(readOnly = true)
    public boolean hasSites() {
        return wrapOperation(siteRepository::hasAnySites, "Ошибка при проверке наличия сайтов", false);
    }

    /**
     * Найти сайт по URL.
     *
     * @param url URL сайта
     * @return Optional SiteEntity
     */
    @Transactional(readOnly = true)
    public Optional<SiteEntity> findSite(String url) {
        return wrapOperation(() -> siteRepository.findByUrl(url), "Ошибка при поиске сайта с url=" + url, Optional.empty());
    }

    /**
     * Найти сайт по ID.
     *
     * @param id идентификатор сайта
     * @return Optional SiteEntity
     */
    @Transactional(readOnly = true)
    public Optional<SiteEntity> findSite(int id) {
        return wrapOperation(() -> siteRepository.findById(id), "Ошибка при поиске сайта с id=" + id, Optional.empty());
    }

    /**
     * Сохранение сайта.
     *
     * @param site объект SiteEntity
     * @return true при успешном сохранении
     */
    @Transactional
    public boolean saveSite(SiteEntity site) {
        return wrapOperation(() -> {
            siteRepository.save(site);
            log.debug("{} Сайт {} сохранён", TAG, site);
            return true;
        }, "Ошибка при сохранении сайта " + site, false);
    }

    /**
     * Удаление сайта по объекту.
     *
     * @param site объект SiteEntity
     * @return true при успешном удалении
     */
    @Transactional
    public boolean deleteSite(SiteEntity site) {
        return wrapOperation(() -> {
            siteRepository.delete(site);
            log.debug("{} Сайт с id={} удалён", TAG, site.getId());
            return true;
        }, "Ошибка при удалении сайта с id=" + site.getId(), false);
    }

    /**
     * Удаление сайта по URL.
     *
     * @param url URL сайта
     * @return true при успешном удалении
     */
    @Transactional
    public boolean deleteSite(String url) {
        return wrapOperation(() -> {
            siteRepository.deleteByUrl(url);
            log.debug("{} Сайт с url={} удалён", TAG, url);
            return true;
        }, "Ошибка при удалении сайта с url=" + url, false);
    }

    /**
     * Удаление сайта по ID.
     *
     * @param id идентификатор сайта
     * @return true при успешном удалении
     */
    @Transactional
    public boolean deleteSiteById(int id) {
        return wrapOperation(() -> {
            siteRepository.deleteById(id);
            log.debug("{} Сайт с id={} удалён", TAG, id);
            return true;
        }, "Ошибка при удалении сайта с id=" + id, false);
    }

    // ==== PAGE METHODS ====

    /**
     * Найти страницу по ID.
     *
     * @param id идентификатор страницы
     * @return Optional PageEntity
     */
    @Transactional(readOnly = true)
    public Optional<PageEntity> findPage(int id) {
        return wrapOperation(() -> pageRepository.findById(id), "Ошибка при поиске страницы с id=" + id, Optional.empty());
    }

    /**
     * Найти страницу по пути.
     *
     * @param path путь страницы
     * @return Optional PageEntity
     */
    @Transactional(readOnly = true)
    public Optional<PageEntity> findPathPage(String path) {
        return wrapOperation(() -> pageRepository.findByPath(path), "Ошибка при поиске страницы по пути " + path, Optional.empty());
    }

    /**
     * Получить все страницы сайта.
     *
     * @param site объект SiteEntity
     * @return список PageEntity
     */
    @Transactional(readOnly = true)
    public List<PageEntity> getAllPagesBySite(SiteEntity site) {
        return wrapOperation(() -> pageRepository.findAllBySiteEntity(site),
                "Ошибка при поиске страниц на сайте " + site.getId(), Collections.emptyList());
    }

    /**
     * Подсчёт страниц сайта.
     *
     * @param site объект SiteEntity
     * @return количество страниц
     */
    @Transactional(readOnly = true)
    public int getCountPagesBySite(SiteEntity site) {
        return wrapOperation(() -> pageRepository.countBySiteId(site.getId()),
                "Ошибка при подсчёте страниц сайта " + site.getId(), 0);
    }

    /**
     * Сохранение страницы.
     *
     * @param page объект PageEntity
     * @return true при успешном сохранении
     */
    @Transactional
    public boolean savePage(PageEntity page) {
        return wrapOperation(() -> {
            pageRepository.save(page);
            log.debug("{} Страница {} сохранена", TAG, page);
            return true;
        }, "Ошибка при сохранении страницы " + page, false);
    }

    /**
     * Удаление страницы по объекту.
     *
     * @param page объект PageEntity
     * @return true при успешном удалении
     */
    @Transactional
    public boolean deletePage(PageEntity page) {
        return wrapOperation(() -> {
            pageRepository.delete(page);
            log.debug("{} Страница с id={} удалена", TAG, page.getId());
            return true;
        }, "Ошибка при удалении страницы с id=" + page.getId(), false);
    }

    /**
     * Удаление страницы по ID.
     *
     * @param id идентификатор страницы
     * @return true при успешном удалении
     */
    @Transactional
    public boolean deletePage(int id) {
        return wrapOperation(() -> {
            pageRepository.deleteById(id);
            log.debug("{} Страница с id={} удалена", TAG, id);
            return true;
        }, "Ошибка при удалении страницы с id=" + id, false);
    }

    // ==== LEMMA METHODS ====

    /**
     * Проверка, есть ли леммы в базе.
     *
     * @return true, если есть хотя бы одна лемма
     */
    @Transactional(readOnly = true)
    public boolean hasLemmas() {
        return wrapOperation(lemmaRepository::hasAnyLemmas, "Ошибка при проверке наличия лемм", false);
    }

    /**
     * Найти лемму по ID.
     *
     * @param id идентификатор леммы
     * @return Optional LemmaEntity
     */
    @Transactional(readOnly = true)
    public Optional<LemmaEntity> findLemma(int id) {
        return wrapOperation(() -> lemmaRepository.findById(id), "Ошибка при поиске леммы с id=" + id, Optional.empty());
    }

    /**
     * Найти лемму по имени и ID сайта.
     *
     * @param lemma имя леммы
     * @param siteId ID сайта
     * @return Optional LemmaEntity
     */
    @Transactional(readOnly = true)
    public Optional<LemmaEntity> findLemma(String lemma, int siteId) {
        return wrapOperation(() -> lemmaRepository.findByLemmaAndSiteId(lemma, siteId),
                "Ошибка при поиске леммы " + lemma + " на сайте " + siteId, Optional.empty());
    }

    /**
     * Найти лемму по имени и объекту сайта.
     *
     * @param lemma имя леммы
     * @param site объект SiteEntity
     * @return Optional LemmaEntity
     */
    @Transactional(readOnly = true)
    public Optional<LemmaEntity> findLemma(String lemma, SiteEntity site) {
        return wrapOperation(() -> lemmaRepository.findByLemmaAndSite(lemma, site),
                "Ошибка при поиске леммы " + lemma + " на сайте " + site.getId(), Optional.empty());
    }

    /**
     * Получить все леммы сайта.
     *
     * @param site объект SiteEntity
     * @return список LemmaEntity
     */
    @Transactional(readOnly = true)
    public List<LemmaEntity> findAllLemmasBySite(SiteEntity site) {
        return wrapOperation(() -> lemmaRepository.findAllBySite(site),
                "Ошибка при поиске лемм на сайте " + site.getId(), Collections.emptyList());
    }

    /**
     * Получить список лемм по именам.
     *
     * @param names список имен
     * @return список LemmaEntity
     */
    @Transactional(readOnly = true)
    public List<LemmaEntity> findLemmas(List<String> names) {
        return wrapOperation(() -> lemmaRepository.findByLemmaIn(names),
                "Ошибка при поиске лемм " + names, Collections.emptyList());
    }

    /**
     * Получить список лемм по именам на конкретном сайте.
     *
     * @param names список имен
     * @param siteUrl URL сайта
     * @return список LemmaEntity
     */
    @Transactional(readOnly = true)
    public List<LemmaEntity> findLemmas(List<String> names, String siteUrl) {
        return wrapOperation(() -> lemmaRepository.findByLemmaInAndSiteEntity_Url(names, siteUrl),
                "Ошибка при поиске лемм " + names + " на сайте " + siteUrl, Collections.emptyList());
    }

    /**
     * Подсчёт лемм на сайте.
     *
     * @param site объект SiteEntity
     * @return количество лемм
     */
    @Transactional(readOnly = true)
    public int getCountLemmasBySite(SiteEntity site) {
        return wrapOperation(() -> lemmaRepository.countLemmasBySite(site.getId()),
                "Ошибка при подсчёте лемм на сайте " + site.getId(), 0);
    }

    /**
     * Сохранение леммы.
     *
     * @param lemma объект LemmaEntity
     * @return true при успешном сохранении
     */
    @Transactional
    public boolean saveLemma(LemmaEntity lemma) {
        return wrapOperation(() -> {
            lemmaRepository.save(lemma);
            log.debug("{} Лемма {} сохранена", TAG, lemma);
            return true;
        }, "Ошибка при сохранении леммы " + lemma, false);
    }

    /**
     * Удаление леммы по ID.
     *
     * @param id идентификатор леммы
     * @return true при успешном удалении
     */
    @Transactional
    public boolean deleteLemma(int id) {
        return wrapOperation(() -> {
            lemmaRepository.deleteById(id);
            log.debug("{} Лемма с id={} удалена", TAG, id);
            return true;
        }, "Ошибка при удалении леммы с id=" + id, false);
    }

    // ==== INDEX METHODS ====

    /**
     * Найти индекс по ID.
     *
     * @param id идентификатор индекса
     * @return Optional IndexEntity
     */
    @Transactional(readOnly = true)
    public Optional<IndexEntity> findIndex(int id) {
        return wrapOperation(() -> indexRepository.findById(id),
                "Ошибка при поиске индекса с id=" + id, Optional.empty());
    }

    /**
     * Получить все индексы леммы на конкретном сайте.
     *
     * @param lemma объект LemmaEntity
     * @param site объект SiteEntity
     * @return список IndexEntity
     */
    @Transactional(readOnly = true)
    public List<IndexEntity> getAllIndexesBySite(LemmaEntity lemma, SiteEntity site) {
        return wrapOperation(() -> indexRepository.findIndexesByLemmaAndSite(lemma, site),
                "Ошибка при поиске индексов на сайте c id=" + site.getId(),
                Collections.emptyList());
    }

    /**
     * Подсчёт страниц, где встречается лемма на сайте.
     *
     * @param lemma объект LemmaEntity
     * @param site объект SiteEntity
     * @return количество страниц
     */
    @Transactional(readOnly = true)
    public int getCountPagesWhereLemma(LemmaEntity lemma, SiteEntity site) {
        return wrapOperation(() -> indexRepository.countPagesContainingLemma(lemma, site),
                "Ошибка при подсчёте страниц на сайте " + site.getId(), 0);
    }

    /**
     * Сохранение одного индекса.
     *
     * @param index объект IndexEntity
     * @return true при успешном сохранении
     */
    @Transactional
    public boolean saveIndex(IndexEntity index) {
        return wrapOperation(() -> {
            indexRepository.save(index);
            log.debug("{} Индекс {} сохранён", TAG, index);
            return true;
        }, "Ошибка при сохранении индекса " + index, false);
    }

    /**
     * Сохранение списка индексов.
     *
     * @param indexes список IndexEntity
     * @return true при успешном сохранении
     */
    @Transactional
    public boolean saveIndex(List<IndexEntity> indexes) {
        return wrapOperation(() -> {
            indexRepository.saveAll(indexes);
            log.debug("{} Список индексов сохранён, count={}", TAG, indexes.size());
            return true;
        }, "Ошибка при сохранении списка индексов", false);
    }

    /**
     * Удаление индекса по ID.
     *
     * @param id идентификатор индекса
     * @return true при успешном удалении
     */
    @Transactional
    public boolean deleteIndex(int id) {
        return wrapOperation(() -> {
            indexRepository.deleteById(id);
            log.debug("{} Индекс с id={} удалён", TAG, id);
            return true;
        }, "Ошибка при удалении индекса с id=" + id, false);
    }
}
