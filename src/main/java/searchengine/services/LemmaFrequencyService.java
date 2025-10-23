package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.search.SearchResult;
import searchengine.logs.LogTag;
import searchengine.model.*;
import searchengine.services.util.EntityFactory;
import searchengine.services.util.SearchBuilder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для работы с леммами и индексами страниц.
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Сохранение лемм и индексов для страницы.</li>
 *   <li>Уменьшение частоты лемм при удалении или обновлении страницы.</li>
 *   <li>Пересчет относительного веса (ранга) лемм для поисковой релевантности.</li>
 *   <li>Поиск страниц по запросу с учетом частот лемм и пересечений.</li>
 * </ul>
 *
 * <p>Используется {@link LemmaProcessor} для генерации лемм,
 * {@link DataManager} для работы с БД и {@link EntityFactory} для создания сущностей.
 */

@Service
@Slf4j
@RequiredArgsConstructor

public class LemmaFrequencyService {

    private static final LogTag TAG = LogTag.LEMMA_FREQUENCY_SERVER;
    private final DataManager dataManager;
    private final LemmaProcessor lemmaProcessor;
    private final EntityFactory entityFactory;
    private static final double PERCENT = 30.0f;

    /**
     * Уменьшает частоты всех лемм, встречающихся на странице.
     * <p>Используется при удалении или обновлении страницы, чтобы поддерживать корректные
     * частоты лемм в БД.
     *
     * @param page страница, для которой нужно уменьшить частоты
     */
    @Transactional
    public void decreaseLemmaFrequencies(Page page) {
        String content = page.getContent();
        Site site = page.getSite();
        if (content == null || content.isBlank()) {
            log.warn("{}  Пустой контент для страницы id={}", TAG, page.getId());
            return;
        }

        Map<String, Integer> lemmas = lemmaProcessor.getLemmas(content);
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            String lemmaName = entry.getKey();
            int countToRemove = entry.getValue();

            dataManager.findLemma(lemmaName, site).ifPresentOrElse(
                    lemmaEntity -> {
                        int newFrequency = lemmaEntity.getFrequency() - countToRemove;
                        lemmaEntity.setFrequency(Math.max(newFrequency, 0));

                        if (lemmaEntity.getFrequency() == 0) {
                            dataManager.deleteLemma(lemmaEntity.getId());
                            log.debug("{}  Удалена лемма '{}'", TAG, lemmaName);
                        } else {
                            dataManager.saveLemma(lemmaEntity);
                        }
                    },
                    () -> log.debug("{}  Лемма '{}' не найдена в БД", TAG, lemmaName)
            );
        }
    }

    /**
     * Потокобезопасная версия {@link #savePageLemmasAndIndexes(Page, String)}
     *
     * @param page страница для индексации
     * @param content контент страницы
     */
    @Transactional
    public void savePageLemmasAndIndexes(Page page, String content) {
        if (content == null || content.isBlank()) {
            log.warn("{}  Пустой контент, сохранение лемм пропущено для страницы id={}", TAG, page.getId());
            return;
        }
        Map<String, Integer> lemmas = lemmaProcessor.getLemmas(content);
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            String lemmaName = entry.getKey();
            int frequencyToAdd = entry.getValue();

            Optional<Lemma> lemmaOpt = dataManager.findLemma(
                    lemmaName,
                    page.getSite().getId()
            );
            Lemma lemma;

            if (lemmaOpt.isEmpty()) {
                lemma = entityFactory.createLemmaEntity(page.getSite(), lemmaName, frequencyToAdd);
                dataManager.saveLemma(lemma);
                log.debug("{}  Создана новая лемма '{}'", TAG, lemmaName);
            } else {
                lemma = lemmaOpt.get();
                lemma.setFrequency(lemma.getFrequency() + frequencyToAdd);
                dataManager.saveLemma(lemma);
            }
            Index index = entityFactory.createIndexEntity(page, lemma, frequencyToAdd);
            dataManager.saveIndex(index);
        }
    }

    /**
     * Потокобезопасная версия {@link #savePageLemmasAndIndexes(Page, String)}
     *
     * @param page страница для индексации
     * @param content контент страницы
     */
    public synchronized void savePageLemmasAndIndexesThreadSafe(Page page, String content) {
        savePageLemmasAndIndexes(page, content);
    }

    /**
     * Пересчитывает вес (ранг) лемм для всех страниц сайта.
     * <p>Используется после полной индексации сайта или при перерасчете релевантности.
     *
     * @param site сайт для пересчета рангов
     */
    @Transactional
    public void recalculateRankForAllSites(Site site) {
        int totalPages = dataManager.getCountPagesBySite(site);
        List<Lemma> lemmas = dataManager.findAllLemmasBySite(site);

        for (Lemma lemma : lemmas) {
            int df = dataManager.getCountPagesWhereLemma(lemma, site);
            List<Index> indexes = dataManager.getAllIndexesBySite(lemma, site);

            for (Index index : indexes) {
                float tf = index.getRank();
                float newRank = (float) (tf * Math.log((double) totalPages / (df + 1)));
                index.setRank(newRank);
            }
            dataManager.saveIndex(indexes);
        }
    }


    /**
     * Получает леммы из БД.
     * <p>Если указан URL, фильтрует по сайту/странице; иначе возвращает все подходящие леммы.
     *
     * @param lemmas список лемм для поиска
     * @param url сайт или конкретная страница (может быть null)
     * @return список сущностей лемм из БД
     */
    private List<Lemma> getLemmaFromDataBase(List<String> lemmas, String url) {
        return (url == null || url.isBlank())
                ? dataManager.findLemmas(lemmas)
                : dataManager.findLemmas(lemmas, url);
    }

    /**
     * Выполняет поиск страниц по запросу.
     *
     * @param query поисковый запрос
     * @param url сайт (если null — поиск по всем сайтам)
     * @param offset смещение для пагинации
     * @param limit максимальное количество результатов
     * @return список {@link SearchResult} с релевантными страницами
     */
    public List<SearchResult> searchResult(String query, String url, int offset, int limit) {
        log.info("{}  Поиск запроса '{}' по сайту '{}'", TAG, query, url);

        List<String> lemmas = lemmaProcessor.getLemmasForSearch(query);
        System.out.println("леммы для запроса " + lemmas);
        if (lemmas.isEmpty()) {
            log.warn("{}  Не найдено лемм для запроса '{}'", TAG, query);
            return List.of();
        }

        List<Lemma> lemmasEntity = getLemmaFromDataBase(lemmas, url);
        if (lemmasEntity.isEmpty()) {
            log.warn("{}  Не найдено лемм в БД для запроса '{}'", TAG, query);
            return List.of();
        }


        List<Lemma> filtered = lemmasEntity.stream()
                .filter(lemma -> {
                    float totalPages = dataManager.getCountPagesBySite(lemma.getSite());
                    float onePercent = totalPages / 100.0f;
                    float lemmaPercent = lemma.getIndexList().size() / onePercent;
                    return lemmaPercent <= PERCENT;
                })
                .sorted(Comparator.comparingInt(Lemma::getFrequency))
                .toList();

        if (filtered.isEmpty()) {
            log.warn("{}  После фильтрации не осталось релевантных лемм", TAG);
            return List.of();
        }

        List<Index> indexes = findIndexesForAllLemmas(filtered, url);
        if (indexes.isEmpty()) {
            log.info("{}  Поиск не дал результатов — пересечение пусто", TAG);
            return List.of();
        }

        Map<Page, Float> absolute = calcAbsoluteRank(indexes);
        Map<Page, Float> relative = calcRelativeRank(absolute, indexes, lemmas);
        SearchBuilder builder = new SearchBuilder();
        List<SearchResult> results = builder.build(relative, offset, limit, query);

        log.info("{}  По запросу '{}' найдено {} результатов", TAG, query, results.size());
        return results;
    }

    /**
     * Находит индексы страниц для всех лемм.
     * <p>Если указан URL, выполняет пересечение страниц по леммам для одного сайта; иначе объединяет все страницы по леммам.
     *
     * @param lemmas список лемм
     * @param url сайт или null
     * @return список индексов страниц
     */
    private List<Index> findIndexesForAllLemmas(List<Lemma> lemmas, String url) {
        if (lemmas.isEmpty()) return List.of();

        if (url != null && !url.isBlank()) {
            List<Index> baseIndexes = new ArrayList<>(lemmas.get(0).getIndexList());
            for (int i = 1; i < lemmas.size(); i++) {
                Set<Integer> pagesWithCurrentLemma = lemmas.get(i).getIndexList().stream()
                        .map(idx -> idx.getPage().getId())
                        .collect(Collectors.toSet());
                baseIndexes = baseIndexes.stream()
                        .filter(idx -> pagesWithCurrentLemma.contains(idx.getPage().getId()))
                        .toList();
            }
            return baseIndexes;
        } else {
            return lemmas.stream()
                    .flatMap(l -> l.getIndexList().stream())
                    .distinct() // убираем дубликаты
                    .toList();
        }
    }

    /**
     * Вычисляет абсолютный ранг страниц.
     * <p>Суммирует веса всех индексов страницы.
     *
     * @param indexes список индексов
     * @return карта страниц и их абсолютного ранга
     */
    private Map<Page, Float> calcAbsoluteRank(List<Index> indexes) {
        Map<Page, Float> pageRanks = new HashMap<>();
        for (Index idx : indexes) {
            pageRanks.merge(idx.getPage(), idx.getRank(), Float::sum);
        }
        log.info("{}  Вычислена абсолютная релевантность для {} страниц", TAG, pageRanks.size());
        return pageRanks;
    }

    /**
     * Вычисляет относительный ранг страниц.
     * <p>Скорректированный ранг учитывает частоту совпадения лемм в запросе и нормализуется по максимальному значению.
     *
     * @param absoluteRanks карта страниц и их абсолютного ранга
     * @param indexes список индексов
     * @param queryLemmas список лемм поискового запроса
     * @return карта страниц и их относительного ранга, отсортированная по убыванию
     */
    private Map<Page, Float> calcRelativeRank(Map<Page, Float> absoluteRanks, List<Index> indexes, List<String> queryLemmas) {
        if (absoluteRanks.isEmpty()) return Map.of();
        Map<Page, Integer> lemmaMatches = new HashMap<>();
        for (Index idx : indexes) {
            lemmaMatches.merge(idx.getPage(), 1, Integer::sum);
        }

        float maxRank = absoluteRanks.values().stream().max(Float::compare).orElse(1.0f);
        Map<Page, Float> relativeRanks = new HashMap<>();

        for (Map.Entry<Page, Float> entry : absoluteRanks.entrySet()) {
            Page page = entry.getKey();
            float base = entry.getValue() / maxRank;
            int matchCount = lemmaMatches.getOrDefault(page, 0);
            float weight = 1.0f + (matchCount / (float) queryLemmas.size());
            relativeRanks.put(page, base * weight);
        }

        return relativeRanks.entrySet().stream()
                .sorted(Map.Entry.<Page, Float>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
    }
}

