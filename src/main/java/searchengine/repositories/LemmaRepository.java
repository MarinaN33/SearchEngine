package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link LemmaEntity}.
 * <p>Содержит методы для поиска, подсчета и выборки лемм по сайтам.</p>
 */

public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {

    /**
     * Находит лемму по тексту и ID сайта.
     *
     * @param lemma текст леммы
     * @param siteId ID сайта
     * @return Optional с найденной леммой
     */
    @Query("SELECT l FROM LemmaEntity l WHERE l.lemma = :lemma AND l.siteEntity.id = :siteId")
    Optional<LemmaEntity> findByLemmaAndSiteId(@Param("lemma") String lemma, @Param("siteId") Integer siteId);

    /**
     * Находит лемму по тексту и объекту сайта.
     *
     * @param lemma текст леммы
     * @param site объект сайта
     * @return Optional с найденной леммой
     */
    @Query("SELECT l FROM LemmaEntity l WHERE l.lemma = :lemma AND l.siteEntity = :site")
    Optional<LemmaEntity> findByLemmaAndSite(@Param("lemma") String lemma, @Param("site") SiteEntity site);


    /**
     * Проверяет, существуют ли какие-либо леммы в базе.
     *
     * @return true, если есть хотя бы одна лемма
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LemmaEntity l")
    boolean hasAnyLemmas();

    /**
     * Получает все леммы для указанного сайта.
     *
     * @param site объект сайта
     * @return список лемм
     */
    @Query("SELECT l FROM LemmaEntity l WHERE l.siteEntity = :site")
    List<LemmaEntity> findAllBySite(@Param("site") SiteEntity site);

    /**
     * Получает все леммы по списку текстов.
     *
     * @param names список текстов лемм
     * @return список лемм
     */
    List<LemmaEntity> findByLemmaIn(List<String> names);

    /**
     * Получает леммы по списку текстов и URL сайта.
     *
     * @param names список текстов лемм
     * @param siteUrl URL сайта
     * @return список лемм
     */
    List<LemmaEntity> findByLemmaInAndSiteEntity_Url(List<String> names, String siteUrl);

    /**
     * Подсчитывает количество лемм для сайта по ID.
     *
     * @param siteId ID сайта
     * @return количество лемм
     */
    @Query("SELECT COUNT(l) FROM LemmaEntity l WHERE l.siteEntity.id = :siteId")
    int countLemmasBySite(@Param("siteId") Integer siteId);
}

