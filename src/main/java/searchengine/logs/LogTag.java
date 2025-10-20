package searchengine.logs;

/**
 * Enum для тегов логов. Используется для унифицированного префикса в логах,
 * чтобы быстро определять подсистему или компонент, откуда пришло сообщение.
 */

public enum LogTag {

    API("API"),
    PAGE_TASK("PAGE-TASK"),
    SITE_TASK("SITE-TASK"),
    MANAGER_TASKS("MANAGER_TASKS"),
    LEMMA("LEMMA"),
    INDEXING("INDEXING"),
    STATISTICS("STATISTICS"),
    RICK_BOT_CLIENT("RBC"),
    INDEXING_CONTEXT("IDX_CTX"),
    JSOUP_MANAGER("JSOUP"),
    INDEXING_SERVER("INDEX-SERVER"),
    LEMMA_FREQUENCY_SERVER("LEMMA-SERVER"),
    DATA_MANAGER("DATA-MANAGER"),
    PAGE_INDEXING_SERVER("PAGE-SERVER"),
    SEARCH_SERVER("SEARCH-SERVER"),
    SEARCH_BUILDER("SEARCH-BUILDER");

    private final String tag;

    LogTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return "[" + tag + "]";
    }
}
