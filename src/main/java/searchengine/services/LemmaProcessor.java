package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.WrongCharaterException;
import org.springframework.stereotype.Service;
import searchengine.logs.LogTag;
import searchengine.services.util.LemmaFilter;
import java.util.*;
import java.util.stream.Stream;

/**
 * Сервис для обработки текста и извлечения лемм.
 *
 * <p>Основные функции класса:
 * <ul>
 *     <li>Токенизация текста на отдельные слова</li>
 *     <li>Очистка и нормализация слов</li>
 *     <li>Получение нормальных форм слов через LuceneMorphology</li>
 *     <li>Подсчет частоты встречаемости лемм</li>
 *     <li>Формирование уникального списка лемм для поисковых запросов</li>
 * </ul>
 *
 * <p>Используется для индексации страниц, подсчета частоты лемм и подготовки данных
 * для поиска по сайту.
 *
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class LemmaProcessor {

    private static final LogTag TAG = LogTag.LEMMA;

    /** Компонент для работы с леммами: токенизация, нормализация, морфология. */
    private final LemmaFilter lemmaFilter;


    /**
     * Генерирует список лемм из текста.
     *
     * @param text исходный текст
     * @return список лемм (может содержать дубликаты)
     */
    private List<String> generateLemmas(String text) {
        return lemmaFilter.tokenizeText(text).stream()
                .map(lemmaFilter::normalizeRussianWord)
                .filter(word -> !word.isEmpty())
                .flatMap(word -> {
                    try {
                        return lemmaFilter.getLuceneMorphology().getNormalForms(word).stream();
                    } catch (WrongCharaterException e) {
                        log.debug("{}  Пропущено некорректное слово для морфологии: {}", TAG, word);
                        return Stream.empty();
                    }
                })
                .toList();
    }

    /**
     * Считает количество вхождений каждой леммы.
     *
     * @param lemmas список лемм
     * @return отображение "лемма -> количество"
     */
    private Map<String, Integer> countLemmas(List<String> lemmas) {
        Map<String, Integer> countMap = new HashMap<>();
        for (String lemma : lemmas) {
            countMap.put(lemma, countMap.getOrDefault(lemma, 0) + 1);
        }
        return countMap;
    }

    /**
     * Получает отображение всех лемм с их частотой для текста.
     *
     * @param text исходный текст
     * @return карта лемма -> количество
     */
    public Map<String, Integer> getLemmas(String text) {
        List<String> lemmas = generateLemmas(text);
        return countLemmas(lemmas);
    }

    /**
     * Получает список уникальных лемм, пригодный для поиска.
     *
     * @param text исходный текст
     * @return список уникальных лемм
     */
    public List<String> getLemmasForSearch(String text) {
        return new ArrayList<>(new LinkedHashSet<>(generateLemmas(text)));
    }
}

