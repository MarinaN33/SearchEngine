package searchengine.services.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import searchengine.model.SiteEntity;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранилище посещённых URL и активных сайтов во время индексации.
 * <p>
 * Использует потокобезопасные коллекции для работы в многопоточном окружении.
 */

@Component
@Getter
@Setter

public class VisitedUrlStore {

    /**
     * Набор посещённых URL. Используется потокобезопасная реализация Set.
     * Ключи добавляются при посещении страниц.
     */
    private final Set<String> visited = ConcurrentHashMap.newKeySet();

    /**
     * Словарь активных сайтов, ключ — URL сайта, значение — объект SiteEntity.
     * Позволяет отслеживать текущие сайты, которые находятся в процессе индексации.
     */
    private final Map<String, SiteEntity> activeSites = new ConcurrentHashMap<>();


    /**
     * Помечает URL как посещённый.
     *
     * @param url URL страницы
     * @return true, если URL был добавлен впервые; false, если URL уже был в наборе
     */
    public boolean visitUrl(String url) {
        return visited.add(url);
    }


    /**
     * Возвращает количество посещённых URL.
     *
     * @return количество уникальных посещённых URL
     */
    public int size() {
        return visited.size();
    }


    /**
     * Помечает сайт как активный.
     *
     * @param site объект SiteEntity сайта
     */
    public void activateSite(SiteEntity site) {
        activeSites.put(site.getUrl(), site);
    }

    /**
     * Убирает сайт из списка активных по URL.
     *
     * @param url URL сайта, который завершил индексацию
     */
    public void markSiteFinished(String url) {
        activeSites.remove(url);
    }

    /**
     * Возвращает коллекцию активных сайтов.
     *
     * @return коллекция объектов SiteEntity активных сайтов
     */
    public Collection<SiteEntity> getActiveSites() {
        return activeSites.values();
    }

    /**
     * Сбрасывает все данные хранилища: посещённые URL и активные сайты.
     */
    public void resetAll() {
        visited.clear();
        activeSites.clear();
    }
}
