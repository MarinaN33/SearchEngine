package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
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
    List<Page> findBySite_Id(int siteId);

    /**
     * Получает все страницы для сайта по объекту сайта.
     *
     * @param site объект сайта
     * @return список страниц
     */
    List<Page> findAllBySite(Site site);

    /**
     * Подсчитывает количество страниц сайта по ID.
     *
     * @param siteId ID сайта
     * @return количество страниц
     */
    int countBySite_Id(int siteId);
}


