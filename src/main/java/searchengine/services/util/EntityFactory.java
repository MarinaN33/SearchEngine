package searchengine.services.util;

import org.springframework.stereotype.Component;
import searchengine.model.*;
import java.time.LocalDateTime;

/**
 * Фабрика сущностей для создания объектов {@link SiteEntity}, {@link PageEntity}, {@link LemmaEntity} и {@link IndexEntity}.
 * <p>Используется для упрощенного создания и инициализации сущностей перед сохранением в базу данных.</p>
 */

@Component
public class EntityFactory {

    /**
     * Создает новый объект {@link SiteEntity} с начальным статусом {@link Status#INDEXING}.
     *
     * @param name имя сайта
     * @param url  URL сайта
     * @return объект {@link SiteEntity}
     */
    public SiteEntity createSiteEntity(String name, String url){
        SiteEntity site = new SiteEntity();
        site.setStatus(Status.INDEXING);
        site.setName(name);
        site.setUrl(url);
        site.setStatusTime(LocalDateTime.now());
        return site;
    }

    /**
     * Создает новый объект {@link PageEntity}.
     *
     * @param siteEntity сайт, которому принадлежит страница
     * @param path       путь страницы
     * @param code       HTTP статус код
     * @param content    HTML содержимое страницы
     * @return объект {@link PageEntity}
     */
    public PageEntity createPageEntity(SiteEntity siteEntity,
                                    String path, int code, String content){
        PageEntity page = new PageEntity();
        page.setSiteEntity(siteEntity);
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        return page;
    }

    /**
     * Создает новый объект {@link LemmaEntity}.
     *
     * @param siteEntity сайт, которому принадлежит лемма
     * @param lemma      текст леммы
     * @param count      частота встречаемости леммы
     * @return объект {@link LemmaEntity}
     */
    public LemmaEntity createLemmaEntity(SiteEntity siteEntity, String lemma, int count){
        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setSiteEntity(siteEntity);
        lemmaEntity.setLemma(lemma);
        lemmaEntity.setFrequency(count);
        return lemmaEntity;
    }

    /**
     * Создает новый объект {@link IndexEntity}.
     *
     * @param pageEntity  страница, к которой относится индекс
     * @param lemmaEntity лемма, к которой относится индекс
     * @param rank        вес леммы на странице
     * @return объект {@link IndexEntity}
     */
    public IndexEntity createIndexEntity(PageEntity pageEntity, LemmaEntity lemmaEntity, float rank){
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setPageEntity(pageEntity);
        indexEntity.setLemmaEntity(lemmaEntity);
        indexEntity.setRank(rank);
        return indexEntity;
    }
}
