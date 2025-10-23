package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.Page;
import searchengine.model.Site;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Page}.
 * <p>Содержит методы для поиска, выборки и подсчета страниц по сайтам.</p>
 */

public interface PageRepository extends JpaRepository<Page, Integer> {

    /**
     * Находит страницу по пути.
     *
     * @param path путь страницы
     * @return Optional с найденной страницей
     */
    Optional<Page> findByPath(String path);

    /**
     * Получает все страницы для сайта по ID.
     *
     * @param siteId ID сайта
     * @return список страниц
     */
    List<Page> findBySiteEntity_Id(int siteId);

    /**
     * Получает все страницы для сайта по объекту сайта.
     *
     * @param site объект сайта
     * @return список страниц
     */
    List<Page> findAllBySiteEntity(Site site);

    /**
     * Подсчитывает количество страниц сайта по ID.
     *
     * @param siteId ID сайта
     * @return количество страниц
     */
    int countBySiteEntity_Id(int siteId);
}
