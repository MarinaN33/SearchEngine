package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link IndexEntity}.
 * <p>Содержит методы для получения индексов лемм и подсчета их встречаемости на сайтах.</p>
 */

public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {

    /**
     * Подсчитывает количество уникальных страниц сайта, где встречается указанная лемма.
     *
     * @param lemma лемма для поиска
     * @param site сайт, на котором ищем лемму
     * @return количество страниц с данной леммой
     */
    @Query("SELECT COUNT(DISTINCT i.pageEntity.id) " +
            "FROM IndexEntity i " +
            "WHERE i.lemmaEntity = :lemma " +
            "AND i.pageEntity.siteEntity = :site")
    int countPagesContainingLemma(@Param("lemma") LemmaEntity lemma,
                                  @Param("site") SiteEntity site);

    /**
     * Получает все индексы указанной леммы на конкретном сайте.
     *
     * @param lemma лемма для поиска
     * @param site сайт, на котором ищем индексы
     * @return список {@link IndexEntity} для данной леммы на сайте
     */
    @Query("SELECT i FROM IndexEntity i " +
            "WHERE i.lemmaEntity = :lemma " +
            "AND i.pageEntity.siteEntity = :site")
    List<IndexEntity> findIndexesByLemmaAndSite(@Param("lemma") LemmaEntity lemma,
                                                @Param("site") SiteEntity site);

}
