package searchengine.services.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.config.RickBotConfig;
import searchengine.logs.LogTag;

import java.io.IOException;
import java.util.Random;

/**
 * Класс для взаимодействия с веб-страницами через HTTP с эмуляцией реального браузера.
 * <p>
 * Основные функции:
 * <ul>
 *     <li>Выбор случайного User-Agent из конфигурации</li>
 *     <li>Добавление случайной задержки между запросами для снижения вероятности блокировок</li>
 *     <li>Получение страницы через Jsoup с обработкой ошибок HTTP и MIME</li>
 * </ul>
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class RickBotClient {

    private static final LogTag TAG = LogTag.RICK_BOT_CLIENT;

    /** Конфигурация клиента RickBot (список User-Agent, задержки, реферер) */
    private final RickBotConfig config;

    /** Генератор случайных чисел для выбора User-Agent и задержки */
    private final Random random = new Random();

    /**
     * Получает веб-страницу по URL с рандомным User-Agent и случайной задержкой.
     * <p>
     * Метод эмулирует поведение реального пользователя, снижая риск блокировки.
     *
     * @param url URL страницы
     * @return объект {@link Document} с содержимым страницы или null, если страница недоступна или MIME не поддерживается
     * @throws IOException если произошла ошибка соединения
     * @throws InterruptedException если поток был прерван во время задержки
     */
    public Document fetchPage(String url) throws IOException, InterruptedException {
        String ua = config.getUserAgents()
                .get(random.nextInt(config.getUserAgents().size()));
        int delay = config.getMinDelayMs() + random.nextInt(Math.max(1, config.getMaxDelayMs() - config.getMinDelayMs() + 1));
        Thread.sleep(delay);
        Connection connection = Jsoup.connect(url)
                .userAgent(ua)
                .referrer(config.getReferer())
                .ignoreHttpErrors(true)
                .timeout(10000);
        try {
            return connection.get();
        } catch (HttpStatusException e) {
            log.warn("{}  HTTP ошибка {} для {}", TAG, e.getStatusCode(), url);
            return null;
        } catch (UnsupportedMimeTypeException e) {
            return null;
        } catch (IOException e) {
            log.error("{}  Ошибка соединения с {}: {}", TAG, url, e.getMessage());
            throw e;
        }
    }
}
