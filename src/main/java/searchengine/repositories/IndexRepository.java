package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Index}.
 * <p>Содержит методы для получения индексов лемм и подсчета их встречаемости на сайтах.</p>
 */

public interface IndexRepository extends JpaRepository<Index, Integer> {

    /**
     * Подсчитывает количество уникальных страниц сайта, где встречается указанная лемма.
     *
     * @param lemma лемма для поиска
     * @param site сайт, на котором ищем лемму
     * @return количество страниц с данной леммой
     */
    int countDistinctByLemmaAndPage_Site(Lemma lemma, Site site);

    /**
     * Получает все индексы указанной леммы на конкретном сайте.
     *
     * @param lemma лемма для поиска
     * @param site сайт, на котором ищем индексы
     * @return список {@link Index} для данной леммы на сайте
     */
    List<Index> findByLemmaAndPage_Site(Lemma lemma, Site site);

}
