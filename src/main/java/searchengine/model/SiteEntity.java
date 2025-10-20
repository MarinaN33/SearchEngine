package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность сайта.
 * <p>Таблица: {@code sites}</p>
 */

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sites")

public class SiteEntity {

    /** Уникальный идентификатор записи. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    /** Статус индексации сайта */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    private Status status;

    /** Время последнего обновления статуса */
    @Column(name = "status_time", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime statusTime;

    /** Текст ошибки последней индексации */
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    /** URL сайта */
    @Column(name = "url", nullable = false, columnDefinition = "VARCHAR(255) ")
    private String url;

    /** Название сайта */
    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

    /** Список страниц сайта */
    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PageEntity> pageEntityList = new ArrayList<>();

    /** Список лемм сайта */
    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<LemmaEntity> lemmaEntityList = new ArrayList<>();
}
