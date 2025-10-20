package searchengine.services.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import searchengine.dto.PageResponse;
import searchengine.logs.LogTag;
import java.io.IOException;
import java.util.*;

/**
 * Компонент для работы с HTML-страницами с использованием Jsoup.
 * <p>Основные функции:
 * <ul>
 *   <li>Получение всех ссылок с указанной страницы, фильтрация по домену.</li>
 *   <li>Загрузка страницы с возвратом кода ответа, HTML-контента и флага "это HTML?".</li>
 *   <li>Очистка HTML от тегов и извлечение текста.</li>
 * </ul>
 * <p>Используется {@link RickBotClient} для получения страницы по URL.
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class ManagerJSOUP {

    private static LogTag TAG = LogTag.JSOUP_MANAGER;
    private final RickBotClient rickBotClient;

    /**
     * Извлекает ссылки со страницы, оставляя только те, что принадлежат указанному домену.
     *
     * @param url URL страницы для сканирования
     * @param siteDomain домен сайта, ссылки вне которого будут игнорироваться
     * @return список ссылок, начинающихся с указанного домена; пустой список при ошибках или отсутствии ссылок
     */
    public List<String> getLinksFromPage(String url, String siteDomain) {
        List<String> links = new ArrayList<>();
        Document doc = null;

        String cleanUrl = url.split("#")[0].toLowerCase();
        if (cleanUrl.matches(".*\\.(jpg|jpeg|png|gif|webp|css|js|svg|ico)$")) {
            return Collections.emptyList();
        }

        try {
            doc = rickBotClient.fetchPage(url);
        } catch (IOException | InterruptedException e) {
            log.warn("{}  Не удалось получить страницу {}: {}", TAG, url, e.getMessage());
        }

        if (doc == null) return Collections.emptyList();

        Elements elements = doc.select("a[href]");
        for (Element el : elements) {
            String absUrl = el.absUrl("href").split("#")[0];
            if (!absUrl.isBlank() && absUrl.startsWith(siteDomain)) {
                links.add(absUrl);
            }
        }
        return links;
    }

    /**
     * Загружает страницу и возвращает {@link PageResponse}, содержащий:
     * <ul>
     *   <li>HTTP статус код</li>
     *   <li>HTML тело страницы, если оно есть</li>
     *   <li>Флаг isHtml — является ли контент HTML</li>
     * </ul>
     *
     * @param url URL страницы
     * @return {@link PageResponse} с данными страницы; при ошибках status=-1, body=null, isHtml=false
     */
    public PageResponse fetchPageWithContent(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .ignoreHttpErrors(true)
                    .execute();

            int status = response.statusCode();
            String body = null;
            boolean isHtml = false;

            String type = response.contentType();
            if (type != null && type.contains("text/html") && status == 200) {
                body = response.body();
                isHtml = true;
            }

            return new PageResponse(status, body, isHtml);

        } catch (IOException e) {
            log.warn("{}  Ошибка загрузки {}: {}", TAG, url, e.getMessage());
            return new PageResponse(-1, null, false);
        }
    }

    /**
     * Удаляет все HTML-теги из строки и возвращает чистый текст.
     *
     * @param html HTML-контент
     * @return текст без тегов; пустая строка, если вход null
     */
    public String stripHtmlTags(String html) {
        return html == null ? "" : Jsoup.parse(html).text();
    }

    /**
     * Извлекает текст из {@link PageResponse}.
     * <p>Возвращает null, если:
     * <ul>
     *   <li>response=null</li>
     *   <li>HTTP статус не 200</li>
     *   <li>тело ответа отсутствует</li>
     * </ul>
     *
     * @param response объект {@link PageResponse}
     * @return текст страницы или null при ошибках
     */
    public String extractText(PageResponse response) {
        if (response == null || response.getStatusCode() != 200 || response.getBody() == null) {
            return null;
        }
        try {
            return Jsoup.parse(response.getBody()).text();
        } catch (Exception e) {
            log.warn("{}  Не удалось извлечь текст из ответа (статус {}): {}", TAG,
                    response.getStatusCode(), e.getMessage());
            return null;
        }
    }
}
