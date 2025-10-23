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
 * Класс для построения результатов поиска.
 * <p>Создает объекты {@link SearchResult} из страниц, учитывая релевантность,
 * выделяет ключевые слова запроса в сниппете и генерирует текстовые превью.
 * <p>Основные функции:
 * <ul>
 *   <li>Формирование списка результатов поиска с пагинацией.</li>
 *   <li>Создание сниппетов с подсветкой слов запроса.</li>
 *   <li>Извлечение заголовков и относительных путей страниц.</li>
 * </ul>
 */

@Slf4j
@NoArgsConstructor

public class SearchBuilder {

    private static final LogTag TAG = LogTag.SEARCH_BUILDER;
    private static final int SNIPPET_RADIUS = 100;
    private static final int SNIPPET_MAX_LENGTH = 250;
    private static final int AVG_LINE_LENGTH = 80;
    private static final int SNIPPET_LINES = 3;

    /**
     * Формирует список {@link SearchResult} на основе карты страниц с релевантностью.
     *
     * @param rankedPages карта страниц и их ранга (релевантности)
     * @param offset смещение для пагинации
     * @param limit максимальное количество результатов
     * @param query поисковый запрос, используется для подсветки слов в сниппете
     * @return список {@link SearchResult} с заполненными сниппетами и заголовками
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
     * Очищает HTML-теги из текста и убирает лишние пробелы.
     *
     * @param content HTML-контент страницы
     * @return чистый текст без тегов
     */
    private String cleanHtmlTags(String content) {
        return content.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }

    /**
     * Строит сниппет текста для отображения в результатах поиска.
     * <p>Выделяет ключевые слова запроса жирным (<b>), ограничивает длину сниппета.
     *
     * @param text текст страницы без HTML
     * @param queryWords список слов из поискового запроса
     * @return сниппет для отображения
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
     * Находит фрагмент текста вокруг первого совпадения слов запроса.
     * <p>Используется внутри {@link #buildSnippet(String, List)}.
     *
     * @param text текст страницы
     * @param queryWords слова запроса
     * @return часть текста с совпадением слова или начало текста
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
     * Создает объект {@link SearchResult} для одной страницы.
     * <p>Извлекает заголовок, относительный путь, сниппет и другие данные.
     *
     * @param page страница
     * @param relevance рейтинг страницы
     * @param queryWords список слов запроса
     * @return {@link SearchResult} для данной страницы
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
     * Извлекает заголовок страницы из HTML-контента.
     *
     * @param html HTML-контент страницы
     * @return заголовок страницы или "(без заголовка)" если не найден
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
     * Вычисляет относительный путь страницы относительно URL сайта.
     *
     * @param fullUrl полный URL страницы
     * @param siteUrl базовый URL сайта
     * @return относительный путь (например, "/index.html") или "/" при ошибках
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
}

