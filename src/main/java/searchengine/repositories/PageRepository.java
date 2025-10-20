package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link PageEntity}.
 * <p>Содержит методы для поиска, выборки и подсчета страниц по сайтам.</p>
 */

public interface PageRepository extends JpaRepository<PageEntity, Integer> {

    /**
     * Находит страницу по пути.
     *
     * @param path путь страницы
     * @return Optional с найденной страницей
     */
    Optional<PageEntity> findByPath(String path);

    /**
     * Получает все страницы для сайта по ID.
     *
     * @param siteId ID сайта
     * @return список страниц
     */
    @Query("SELECT p FROM PageEntity p WHERE p.siteEntity.id = :siteId")
    List<PageEntity> findAllBySiteId(@Param("siteId") int siteId);

    /**
     * Получает все страницы для сайта по объекту сайта.
     *
     * @param siteEntity объект сайта
     * @return список страниц
     */
    List<PageEntity> findAllBySiteEntity(SiteEntity siteEntity);

    /**
     * Подсчитывает количество страниц сайта по ID.
     *
     * @param siteId ID сайта
     * @return количество страниц
     */
    @Query("SELECT COUNT(p) FROM PageEntity p WHERE p.siteEntity.id = :siteId")
    int countBySiteId(@Param("siteId") int siteId);
}
