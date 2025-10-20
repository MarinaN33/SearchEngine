package searchengine.services.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * Компонент для предварительной обработки текста и фильтрации слов.
 *
 * <p>Основные функции класса:
 * <ul>
 *     <li>Удаление HTML-тегов из текста через {@link ManagerJSOUP}</li>
 *     <li>Токенизация текста на отдельные слова</li>
 *     <li>Нормализация слов: приведение к нижнему регистру и удаление лишних символов</li>
 *     <li>Фильтрация служебных частей речи (союзы, междометия, частицы и т.д.) с использованием LuceneMorphology</li>
 *     <li>Проверка, что слово является корректным русским словом</li>
 * </ul>
 *
 * <p>Используется для подготовки текста перед извлечением лемм и индексацией.
 */

@Component
@Getter
@RequiredArgsConstructor

public class LemmaFilter {

    private final ManagerJSOUP managerJSOUP;
    private LuceneMorphology luceneMorphology;
    private static final Set<String> SERVICE_POS = Set.of(
            "СОЮЗ", "МЕЖД", "ЧАСТ", "ПРЕД", "МС", "МС-П", "МС-С", "INTJ"
    );

    @PostConstruct
    private void init() {
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка инициализации LuceneMorphology", e);
        }
    }

    /**
     * Разбивает текст на отдельные слова и фильтрует служебные части речи.
     *
     * @param text исходный HTML или текст
     * @return список слов, пригодных для дальнейшей обработки
     */
    public List<String> tokenizeText(String text) {
        List<String> words = prepareText(text);

        return words.stream()
                .filter(this::isNotServiceWord)
                .toList();
    }

    /**
     * Проверяет, является ли слово служебным (союз, междометие, частица и т.д.).
     *
     * @param word слово для проверки
     * @return true, если слово НЕ является служебным
     */
    private boolean isNotServiceWord(String word) {
        word = normalizeRussianWord(word);
        if(isValidRussianWord(word)) {
            List<String> morphInfo = luceneMorphology.getMorphInfo(word);
            return morphInfo.stream().noneMatch(info ->
                    SERVICE_POS.stream().anyMatch(info::contains)
            );
        }
        return false;
    }

    /**
     * Удаляет HTML-теги из текста через {@link ManagerJSOUP}.
     *
     * @param text исходный HTML
     * @return текст без HTML-тегов
     */
    private String filterTagHtml(String text) {
        return managerJSOUP.stripHtmlTags(text);
    }

    /**
     * Подготавливает текст для токенизации: убирает лишние символы, переводит в нижний регистр,
     * фильтрует латиницу и пустые слова.
     *
     * @param html исходный HTML или текст
     * @return список "чистых" слов
     */
    private List<String> prepareText(String html) {
        String text = filterTagHtml(html);

        return Arrays.stream(text.split("\\s+"))
                .map(word -> word.strip()
                        .replaceAll("[^\\p{L}]", "")
                        .toLowerCase())
                .filter(word -> !word.isEmpty())
                .filter(word -> !word.matches("[a-z]+"))
                .toList();
    }

    /**
     * Нормализует русское слово: приведение к нижнему регистру и удаление лишних символов.
     *
     * @param word слово для нормализации
     * @return нормализованная форма слова
     */
    public String normalizeRussianWord(String word) {
        return word.toLowerCase()
                .replaceAll("[^а-яё]", "");
    }

    /**
     * Проверяет, является ли слово корректным русским словом.
     *
     * @param word слово для проверки
     * @return true, если слово состоит только из букв а-я или ё
     */
    public boolean isValidRussianWord(String word) {
        return word.matches("[а-яё]+");
    }
}

