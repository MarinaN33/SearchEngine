package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.List;

/**
 * Сущность леммы (основной формы слова), используемая в индексации сайта.
 * <p>Таблица: {@code lemmas}</p>
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "lemmas")
public class LemmaEntity {

    /** Уникальный идентификатор записи. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    /** Сайт, к которому относится лемма */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity siteEntity;

    /** Текст леммы (например: "поиск") */
    @Column(name = "lemma", nullable = false, columnDefinition = "VARCHAR(255)")
    private String lemma;

    /** Частота встречаемости леммы на сайте */
    @Column(name = "frequency", nullable = false)
    private int frequency;

    /** Список индексов, где используется данная лемма */
    @OneToMany(mappedBy = "lemmaEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IndexEntity> indexEntityList;
}
