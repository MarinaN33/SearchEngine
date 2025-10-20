package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс запуска Spring Boot приложения.
 * <p>
 * Основные функции:
 * <ul>
 *     <li>Точка входа в приложение</li>
 *     <li>Автоматическая конфигурация Spring Boot через {@link SpringBootApplication}</li>
 * </ul>
 */

@SpringBootApplication
public class Application {
    public static void main(String[] args) {

        /**
         * Точка входа в приложение Spring Boot.
         * <p>
         * Метод вызывает {@link SpringApplication#run(Class, String[])} для запуска контекста Spring.
         *
         * @param args аргументы командной строки (опционально)
         */
        SpringApplication.run(Application.class, args);
    }
}
