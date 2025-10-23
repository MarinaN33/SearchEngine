package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Site}.
 * <p>Содержит методы для поиска, удаления и проверки существования сайтов.</p>
 */

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {

    /**
     * Находит сайт по его URL.
     *
     * @param url URL сайта
     * @return Optional с найденным сайтом
     */
    Optional<Site> findByUrl(String url);

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
    boolean existsBy();
}