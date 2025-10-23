package searchengine.services.util;

import org.springframework.stereotype.Component;
import searchengine.model.*;
import java.time.LocalDateTime;

/**
 * Фабрика сущностей для создания объектов {@link Site}, {@link Page}, {@link Lemma} и {@link Index}.
 * <p>Используется для упрощенного создания и инициализации сущностей перед сохранением в базу данных.</p>
 */

@Component
public class EntityFactory {

    /**
     * Создает новый объект {@link Site} с начальным статусом {@link Status#INDEXING}.
     *
     * @param name имя сайта
     * @param url  URL сайта
     * @return объект {@link Site}
     */
    public Site createSiteEntity(String name, String url){
        Site site = new Site();
        site.setStatus(Status.INDEXING);
        site.setName(name);
        site.setUrl(url);
        site.setStatusTime(LocalDateTime.now());
        return site;
    }

    /**
     * Создает новый объект {@link Page}.
     *
     * @param site сайт, которому принадлежит страница
     * @param path       путь страницы
     * @param code       HTTP статус код
     * @param content    HTML содержимое страницы
     * @return объект {@link Page}
     */
    public Page createPageEntity(Site site,
                                 String path, int code, String content){
        Page page = new Page();
        page.setSite(site);
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        return page;
    }

    /**
     * Создает новый объект {@link Lemma}.
     *
     * @param site сайт, которому принадлежит лемма
     * @param lemma      текст леммы
     * @param count      частота встречаемости леммы
     * @return объект {@link Lemma}
     */
    public Lemma createLemmaEntity(Site site, String lemma, int count){
        Lemma lemmaEntity = new Lemma();
        lemmaEntity.setSite(site);
        lemmaEntity.setLemma(lemma);
        lemmaEntity.setFrequency(count);
        return lemmaEntity;
    }

    /**
     * Создает новый объект {@link Index}.
     *
     * @param page  страница, к которой относится индекс
     * @param lemma лемма, к которой относится индекс
     * @param rank        вес леммы на странице
     * @return объект {@link Index}
     */
    public Index createIndexEntity(Page page, Lemma lemma, float rank){
        Index index = new Index();
        index.setPage(page);
        index.setLemma(lemma);
        index.setRank(rank);
        return index;
    }
}
