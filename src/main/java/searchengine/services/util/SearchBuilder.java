package searchengine.services.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.dto.search.SearchResult;
import searchengine.logs.LogTag;
import searchengine.model.Page;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс {@code SearchBuilder} отвечает за построение результатов поиска.
 * <p>Основные задачи:
 * <ul>
 *     <li>Формирование списка {@link SearchResult} с учетом релевантности.</li>
 *     <li>Создание сниппетов (фрагментов текста) с подсветкой слов запроса.</li>
 *     <li>Извлечение заголовков, относительных путей и других метаданных страницы.</li>
 * </ul>
 * <p>Поддерживает два режима формирования сниппета:
 * <ul>
 *     <li>По словам запроса — {@link #buildSnippet(String, List)}</li>
 *     <li>По леммам — {@link #buildSnippet(List, String)}</li>
 * </ul>
 */
@Slf4j
@NoArgsConstructor
public class SearchBuilder {

    private static final LogTag TAG = LogTag.SEARCH_BUILDER;

    /** Количество символов влево и вправо от найденного слова при построении сниппета. */
    private static final int SNIPPET_RADIUS = 100;

    /** Максимальная длина сниппета. */
    private static final int SNIPPET_MAX_LENGTH = 250;

    /** Средняя длина строки для визуального ограничения длины сниппета. */
    private static final int AVG_LINE_LENGTH = 80;

    /** Количество строк, отображаемых в сниппете. */
    private static final int SNIPPET_LINES = 3;

    /**
     * Формирует список результатов поиска.
     *
     * @param rankedPages карта страниц и их релевантности
     * @param offset смещение (для пагинации)
     * @param limit максимальное количество элементов
     * @param query поисковый запрос
     * @return список объектов {@link SearchResult}
     */
    public List<SearchResult> build(Map<Page, Float> rankedPages, int offset, int limit, String query) {
        if (rankedPages.isEmpty()) return List.of();

        List<String> queryWords = Arrays.stream(query.toLowerCase().split("\\s+"))
                .filter(s -> s.length() > 1)
                .toList();

        return rankedPages.entrySet().stream()
                .skip(offset)
                .limit(limit)
                .map(entry -> createSearchResult(entry.getKey(), entry.getValue(), queryWords))
                .toList();
    }

    /**
     * Удаляет HTML-теги из текста.
     *
     * @param content HTML-контент страницы
     * @return очищенный текст
     */
    private String cleanHtmlTags(String content) {
        return content.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }

    /**
     * Строит сниппет на основе текста и слов запроса.
     * <p>Использует регулярные выражения для подсветки слов запроса.
     *
     * @param text очищенный текст страницы
     * @param queryWords список слов поискового запроса
     * @return сниппет с подсветкой совпадений
     */
    private String buildSnippet(String text, List<String> queryWords) {
        if (text.isBlank()) return "";

        String snippet = getString(text, queryWords);

        if (!queryWords.isEmpty()) {
            Pattern pattern = Pattern.compile("\\b(" + String.join("|", queryWords) + ")\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(snippet);
            snippet = matcher.replaceAll("<b>$1</b>");
        }

        int maxLen = AVG_LINE_LENGTH * SNIPPET_LINES;
        snippet = snippet.length() > maxLen ? snippet.substring(0, maxLen) + "..." : snippet;
        return snippet;
    }

    /**
     * Извлекает часть текста, где впервые встречается одно из слов запроса.
     *
     * @param text текст страницы
     * @param queryWords слова запроса
     * @return фрагмент текста вокруг первого совпадения
     */
    private static String getString(String text, List<String> queryWords) {
        String lowerText = text.toLowerCase();
        int matchIndex = -1;

        for (String word : queryWords) {
            int idx = lowerText.indexOf(word);
            if (idx >= 0) {
                matchIndex = idx;
                break;
            }
        }
        int start = matchIndex == -1 ? 0 : Math.max(0, matchIndex - SNIPPET_RADIUS);
        int end = matchIndex == -1
                ? Math.min(SNIPPET_MAX_LENGTH, text.length())
                : Math.min(text.length(), matchIndex + SNIPPET_RADIUS);
        return text.substring(start, end);
    }

    /**
     * Создает объект {@link SearchResult} на основе страницы и её релевантности.
     *
     * @param page страница
     * @param relevance значение релевантности
     * @param queryWords слова поискового запроса
     * @return объект {@link SearchResult}
     */
    private SearchResult createSearchResult(Page page, float relevance, List<String> queryWords) {
        String siteUrl = Optional.ofNullable(page.getSite())
                .map(s -> s.getUrl())
                .orElse("");
        String siteName = Optional.ofNullable(page.getSite())
                .map(s -> s.getName())
                .orElse("(без имени)");
        String pagePath = Optional.ofNullable(page.getPath()).orElse("");
        String uri = pagePath.startsWith("http") ? extractRelativePath(pagePath, siteUrl) : pagePath;
        String content = Optional.ofNullable(page.getContent()).orElse("");
        String title = extractTitleFromHtml(content);
        String text = cleanHtmlTags(content);
        String snippet = buildSnippet(text, queryWords);

        return new SearchResult(siteUrl, siteName, uri, title, snippet, relevance);
    }

    /**
     * Извлекает заголовок страницы из HTML-кода.
     *
     * @param html HTML-контент
     * @return текст заголовка или "(без заголовка)"
     */
    private String extractTitleFromHtml(String html) {
        if (html == null || html.isBlank()) return "(без заголовка)";
        Pattern pattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            String title = matcher.group(1).replaceAll("\\s+", " ").trim();
            return title.isEmpty() ? "(без заголовка)" : title;
        }
        return "(без заголовка)";
    }

    /**
     * Извлекает относительный путь страницы относительно базового URL сайта.
     *
     * @param fullUrl полный URL страницы
     * @param siteUrl базовый URL сайта
     * @return относительный путь (например, "/about")
     */
    private String extractRelativePath(String fullUrl, String siteUrl) {
        try {
            if (fullUrl.startsWith(siteUrl)) {
                String relative = fullUrl.substring(siteUrl.length());
                return relative.isEmpty() ? "/" : (relative.startsWith("/") ? relative : "/" + relative);
            }
            return "/";
        } catch (Exception e) {
            log.warn("{}  Не удалось извлечь относительный путь из {}", TAG, fullUrl, e);
            return "/";
        }
    }

    /**
     * Создает сниппет на основе списка лемм.
     * <p>Метод ищет в тексте первые совпадения с леммами (по частичному совпадению первых 3–4 символов)
     * и возвращает фрагмент текста вокруг найденного участка.
     * <p>Используется, когда нужно учитывать морфологию (например, разные формы слова "поиск").
     *
     * @param lemmas список лемм (например, ["поиск", "система"])
     * @param text исходный текст страницы
     * @return сниппет текста, содержащий одно из слов-лемм
     */
    public String buildSnippet(List<String> lemmas, String text) {
        if (text == null || text.isBlank()) return "";
        return buildSnippetRecursive(lemmas, text.toLowerCase(), 0);
    }

/**
 * Рекурсивно ищет первую лемму, встречающуюся в тексте, и возвращает
 * сниппет, который НАЧИНАЕТСЯ с найденного слова.
 * Поддерживает частичные совпадения (по первым 3–4 символам).
 *
 * @param lemmas список лемм
 * @param text исходный текст страницы (в нормальном регистре)
 * @param index индекс текущей леммы
 * @return сниппет, начинающийся с найденного слова
 */
private String buildSnippetRecursive(List<String> lemmas, String text, int index) {
    if (index >= lemmas.size()) {
        // Ничего не нашли — возвращаем начало текста
        return text.substring(0, Math.min(text.length(), SNIPPET_MAX_LENGTH));
    }

    String lemma = lemmas.get(index).toLowerCase();
    String pattern = createPattern(lemma);
    String lowerText = text.toLowerCase();

    int matchIndex = lowerText.indexOf(pattern);
    if (matchIndex != -1) {
        // Сниппет начинается С НАЙДЕННОГО слова
        int start = matchIndex;
        int end = Math.min(text.length(), start + SNIPPET_MAX_LENGTH);

        // Вырезаем фрагмент из оригинального текста (с сохранением регистра)
        String snippet = text.substring(start, end).trim();

        // Подсвечиваем совпадение
        Pattern highlightPattern = Pattern.compile("(?i)" + Pattern.quote(pattern));
        Matcher matcher = highlightPattern.matcher(snippet);
        snippet = matcher.replaceAll("<b>$0</b>");

        return snippet;
    }

    // Ищем следующую лемму
    return buildSnippetRecursive(lemmas, text, index + 1);
}


    /**
     * Создает шаблон для поиска по лемме.
     * <p>Использует первые 3–4 символа леммы, чтобы захватывать разные формы слова.
     *
     * @param lemma лемма слова
     * @return строковый шаблон для поиска
     */
    private String createPattern(String lemma) {
        int len = Math.min(lemma.length(), 4);
        return lemma.substring(0, len);
    }
}


