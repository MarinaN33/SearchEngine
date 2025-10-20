package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link SiteEntity}.
 * <p>Содержит методы для поиска, удаления и проверки существования сайтов.</p>
 */

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {

    /**
     * Находит сайт по его URL.
     *
     * @param url URL сайта
     * @return Optional с найденным сайтом
     */
    Optional<SiteEntity> findByUrl(String url);

    /**
     * Удаляет сайт по URL.
     *
     * @param url URL сайта
     */
    void deleteByUrl(String url);

    /**
     * Проверяет, существуют ли какие-либо сайты в базе.
     *
     * @return true, если есть хотя бы один сайт
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SiteEntity s")
    boolean hasAnySites();
}