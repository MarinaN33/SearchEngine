package searchengine.model;

import lombok.*;

import javax.persistence.*;

/**
 * Сущность, описывающая связь между страницей и леммой.
 * <p>
 * Таблица {@code search_indexes} хранит весовые коэффициенты (rank),
 * показывающие значимость леммы на конкретной странице.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="search_indexes")

public class IndexEntity {

    /** Уникальный идентификатор записи. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Ссылка на страницу, где встречается лемма. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_id", nullable = false)
    private PageEntity pageEntity;

    /** Ссылка на лемму, встречающуюся на странице. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lemma_id", nullable = false)
    private LemmaEntity lemmaEntity;

    /** Вес (значимость) леммы на данной странице. */
    @Column(name = "rank_value", nullable = false)
    private float rank;
}
