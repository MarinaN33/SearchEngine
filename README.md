# SearchEngine

**Поисковый движок на Java с использованием Spring Boot и MySQL**

SearchEngine — это Spring Boot приложение, реализующее полнотекстовый поиск по локально индексированным страницам сайтов. Система поддерживает лемматизацию русского, API для получения результатов поиска.

Документация API:
Каждый метод проекта снабжен подробной документацией, а для удобного просмотра и тестирования API используется Springdoc OpenAPI UI. Для этого подключена зависимость: springdoc-openapi-ui.

---

## Технологический стек

- **Язык:** Java 17
- **Фреймворк:** Spring Boot 2.7.1
- **HTML-парсер:** Jsoup 1.16.1
- **ORM:** Spring Data JPA
- **База данных:** MySQL
- **Управление зависимостями:** Maven
- **Логирование и утилиты:** Lombok 1.18.32, SLF4J
- **Миграции БД:** Liquibase
- **Шаблоны (если веб-интерфейс):** Thymeleaf

---
## Maven и зависимости (pom.xml)  
Проект использует Maven для управления зависимостями и сборки:  
spring-boot-starter-web — для REST API и веб-функционала.  
spring-boot-starter-data-jpa — работа с базой данных через JPA/Hibernate.  
spring-boot-starter-validation — валидация данных входящих запросов.  
spring-boot-starter-thymeleaf — шаблонизация страниц (если веб-интерфейс).  
jsoup — парсинг HTML страниц и извлечение текста/тегов.  
mysql-connector-java — драйвер MySQL.  
lombok — сокращение boilerplate-кода (@Getter, @Setter, @Slf4j).  
Apache Lucene Morphology — лемматизация русского языка   
Liquibase — управление версионированием базы данных.  

## Конфигурация приложения (`application.yml`)

Ниже пример конфигурации с пояснениями:

```yaml
server:
  port: 8080 # Порт, на котором будет запущен Spring Boot сервер

logging:
  level:
    org.apache.coyote.http11.Http11Processor: ERROR # Убираем шум логов Tomcat

# Настройки RickBot — робота, который обходит сайты и индексирует страницы
rickbot:
  user-agents:
    - "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    - "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)"
    - "RickBot/1.0"
  min-delay-ms: 500 # минимальная задержка между запросами
  max-delay-ms: 2000 # максимальная задержка между запросами
  referer: "https://github.com/yourusername/searchengine/blob/main/README.md"

spring:
  liquibase:
    change-log: classpath:db/changelog/changelog-master.xml # Скрипты миграции базы
  datasource:
    username: root
    password: password_here
    url: jdbc:mysql://localhost:3306/search_engine?useSSL=false&allowPublicKeyRetrieval=true
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
    hibernate:
      ddl-auto: none # управление схемой БД (используется Liquibase)
    show-sql: false # для отладки — вывод SQL-запросов

# Настройки сайтов для индексирования
indexing-settings:
  siteEntities:
    - url: https://nikoartgallery.com/
      name: Nikoargallery.com
    - url: https://www.playback.ru
      name: PlayBack.Ru
🔹 Комментарии помогают понять, что делает каждый блок и как менять конфигурацию под свои сайты и БД.

Структура проекта
src/
 └─ main/
     ├─ java/...          # Основной код приложения
     └─ resources/
         ├─ application.yml       # Конфигурации Spring и базы данных
         └─ db/changelog/          # Скрипты Liquibase
.gitignore                      # Исключения для IDE, .class, target
README.md                       # Документация проекта
pom.xml                         # Maven-конфигурация и зависимости

```
### Сборка и запуск
1. Клонирование репозитория
```bash
git clone https://github.com/yourusername/searchengine.git
cd searchengine
```
2. Настроить базу данных MySQL:
```sql
CREATE DATABASE search_engine;
```
4. Сборка проекта
```bash
mvn clean install
```
5. Запуск
```bash
mvn spring-boot:run
```
После запуска API будет доступен на http://localhost:8080

 Миграции базы данных (Liquibase)
Перед использованием проекта убедитесь, что база данных создана и доступна.
Пример конфигурации в application.yml для Liquibase:
spring:
  liquibase:
    change-log: classpath:db/changelog/changelog-master.xml
Файл src/main/resources/db/changelog/changelog-master.xml содержит версионированные изменения схемы, например:  
```html
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <!-- 1. Изменяем тип колонки path на TEXT -->
    <changeSet id="1" author="rik">
        <modifyDataType tableName="pages" columnName="path" newDataType="TEXT"/>
    </changeSet>

    <!-- 2. Создаём индекс по path, если его нет -->
    <changeSet id="2" author="rik">
        <preConditions onFail="MARK_RAN">
            <not>
                <indexExists indexName="idx_path" tableName="pages"/>
            </not>
        </preConditions>
        <createIndex indexName="idx_path" tableName="pages">
            <column name="path" type="varchar(255)"/>
        </createIndex>
    </changeSet>

    <!-- 3. Альтернатива через SQL -->
    <changeSet id="3" author="rik">
        <preConditions onFail="MARK_RAN">
            <not>
                <indexExists indexName="idx_path" tableName="pages"/>
            </not>
        </preConditions>
        <modifyDataType tableName="pages" columnName="path" newDataType="TEXT"/>
        <sql>
            CREATE INDEX idx_path ON pages (path(255));
        </sql>
    </changeSet>

</databaseChangeLog>
```
💡 Примечания:
Liquibase автоматически применяет все изменения при старте приложения.
Если колонка или индекс уже существуют, изменения пропускаются (onFail="MARK_RAN").
Такой подход обеспечивает безопасное версионирование схемы базы данных и готовность проекта к развертыванию.
---
# Дерево файлов проекта  
```java
searchengine  
├─ config/    # Конфигурации приложения  
│   ├─ RickBotConfig.java    # Настройки бота обхода сайтов  
│   ├─ Site.java  
│   └─ SiteList.java  
├─ controllers/  # REST API и веб-контроллеры  
│   ├─ ApiController.java  
│   └─ DefaultController.java  
├─ dto/      # DTO объекты для передачи данных  
│   ├─ ApiResponse.java  
│   ├─ PageResponse.java  
|   ├─  statistics/  
|   |   ├─ DetailedStatisticsItem.java  
|   |   ├─ StatisticsData.java  
│   |   ├─ StatisticsResponse.java  
│   |   └─ TotalResponse.java  
|   └─ earch/  
│      ├─ SearchResponse.java  
│      └─ SearchResult.java  
├─ model/          # Сущности базы данных  
│   ├─ PageEntity.java  
│   ├─ LemmaEntity.java  
│   ├─ IndexEntity.java  
│   ├─ SiteEntity.java  
│   └─ Status.java  
├─ repositories/  # JPA репозитории  
│   ├─ PageRepository.java  
│   ├─ LemmaRepository.java  
│   ├─ IndexRepository.java  
│   └─ SiteRepository.java
├─ log
|   └─ LogTag.java
├─ services/   # Реализация бизнес-логики  
│   ├─ IndexingServiceImpl.java  
│   ├─ PageIndexingServiceImpl.java  
│   ├─ SearchServiceImpl.java  
│   ├─ StatisticsServiceImpl.java  
│   ├─ LemmaProcessor.java  
│   ├─ LemmaFrequencyService.java  
│   ├─ DataManager.java  
│   ├─ ManagerTasks.java  
|   ├─ serviceinterface/   # Интерфейсы сервисов  
│   |  ├─ IndexingService.java  
│   |  ├─ PageIndexingService.java  
│   |  ├─ SearchService.java  
│   |  └─ StatisticsService.java  
|   ├─ tasks/   # Классы задач для многопоточной индексации  
│   |  ├─ PageTask.java  
│   |  ├─ SiteTask.java  
│   |  └─ SitesTask.java  
|   └─ utils/   # Вспомогательные классы и утилиты  
│      ├─ EntityFactory.java  
│      ├─ IndexingContext.java  
│      ├─ LemmaFilter.java  
│      ├─ ManagerJSOUP.java  
│      ├─ RickBotClient.java  
│      ├─ SearchBuilder.java  
│      ├─ Stopwatch.java  
│      └─ VisitedUrlStore.java  
├─ Application.java   # Точка входа Spring Boot  
└─ resources/  
    ├─ db/changelog/   # Скрипты Liquibase  
    │   └─ changelog-master.xml  
    ├─ static/   # CSS, шрифты, фронтенд ресурсы  
    │   └─ assets  
    │      ├─ css/  
    │      ├─ fonts/  
    |      ├─ img/  
    |      ├─ js/  
    |      └─ plg/  
    ├─ templates/  # Шаблоны Thymeleaf  
    │   └─ index.html  
    └─ application.yml  # Конфигурация приложения  
```
## Как пользоваться API
---
Все методы доступны по базовому пути: http://localhost:8080/api. Ниже приведены основные эндпоинты и примеры использования.
1. Запуск индексации всех сайтов
GET /api/startIndexing
Пример запроса:
curl -X GET http://localhost:8080/api/startIndexing
Ответ:

```json
{
  "result": true,
  "error": null
}
```
2. Остановка индексации
GET /api/stopIndexing
Пример запроса:
curl -X GET http://localhost:8080/api/stopIndexing
3. Получение статистики
GET /api/statistics
Пример запроса:
curl -X GET http://localhost:8080/api/statistics
Пример ответа:

```json
{
  "totalPages": 120,
  "totalLemmas": 4500,
  "siteEntities": [
    {
      "url": "https://nikoartgallery.com",
      "status": "INDEXED",
      "pages": 70
    },
    {
      "url": "https://www.playback.ru",
      "status": "INDEXED",
      "pages": 50
    }
  ]
}
```
4. Индексация конкретной страницы
POST /api/indexPage?url={URL}
Пример запроса:
curl -X POST "http://localhost:8080/api/indexPage?url=https://nikoartgallery.com/art1"
5. Поиск по сайту или запросу
GET /api/search?query={запрос}&site={сайт}&offset=0&limit=20
Пример запроса:
curl -X GET "http://localhost:8080/api/search?query=картина&site=https://nikoartgallery.com"
Пример ответа:

```json
{
  "result": true,
  "count": 5,
  "data": [
    {
      "title": "Картина «Закат»",
      "snippet": "Картина «Закат» выполнена маслом на холсте. Это одна из самых известных работ художника...",
      "url": "https://nikoartgallery.com/art1"
    },
    {
      "title": "Картина «Утро»",
      "snippet": "Утренний пейзаж отражает нежные оттенки неба и света, создавая атмосферу спокойствия...",
      "url": "https://nikoartgallery.com/art2"
    }
  ]
}
```
