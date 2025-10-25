package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Lemma}.
 * <p>Содержит методы для поиска, подсчета и выборки лемм по сайтам.</p>
 */

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    /**
     * Находит лемму по тексту и ID сайта.
     *
     * @param lemma текст леммы
     * @param siteId ID сайта
     * @return Optional с найденной леммой
     */
    Optional<Lemma> findByLemmaAndSite_Id(String lemma, Integer siteId);

    /**
     * Находит лемму по тексту и объекту сайта.
     *
     * @param lemma текст леммы
     * @param site объект сайта
     * @return Optional с найденной леммой
     */
    Optional<Lemma> findByLemmaAndSite(String lemma, Site site);


    /**
     * Проверяет, существуют ли какие-либо леммы в базе.
     *
     * @return true, если есть хотя бы одна лемма
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Lemma l")
    boolean hasAnyLemmas();

    /**
     * Получает все леммы для указанного сайта.
     *
     * @param site объект сайта
     * @return список лемм
     */
    List<Lemma> findBySite(Site site);

    /**
     * Получает все леммы по списку текстов.
     *
     * @param names список текстов лемм
     * @return список лемм
     */
    List<Lemma> findByLemmaIn(List<String> names);

    /**
     * Получает леммы по списку текстов и URL сайта.
     *
     * @param names список текстов лемм
     * @param siteUrl URL сайта
     * @return список лемм
     */
    List<Lemma> findByLemmaInAndSite_Url(List<String> names, String siteUrl);

    /**
     * Подсчитывает количество лемм для сайта по ID.
     *
     * @param siteId ID сайта
     * @return количество лемм
     */
    int countBySite_Id(Integer siteId);
}

