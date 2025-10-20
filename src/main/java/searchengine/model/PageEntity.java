package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.List;

/**
 * Сущность страницы сайта.
 * <p>Таблица: {@code pages}</p>
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "pages", indexes = {
        @Index(name = "idx_path", columnList = "path")
})

public class PageEntity {

    /** Уникальный идентификатор записи. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Сайт, которому принадлежит страница */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity siteEntity;

    /** Путь страницы (например: "/about") */
    @Column(name = "path", nullable = false, columnDefinition = "TEXT")
    private String path;

    /** HTTP-код ответа страницы */
    @Column(name = "code", nullable = false)
    private int code;

    /** HTML-контент страницы */
    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    /** Индексы (леммы и их веса), связанные с этой страницей */
    @OneToMany(mappedBy = "pageEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IndexEntity> indexEntityList;
}
