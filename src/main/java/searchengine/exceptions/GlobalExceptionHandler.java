package searchengine.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import searchengine.dto.ApiResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse handleBadRequest(BadRequestException ex) {
        log.warn("Ошибка 400: {}", ex.getMessage());
        return new ApiResponse(false, ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse handleNotFound(NotFoundException ex) {
        log.warn("Ошибка 404: {}", ex.getMessage());
        return new ApiResponse(false, ex.getMessage());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse handleInternalServerError(InternalServerErrorException ex) {
        log.error("Ошибка 500: {}", ex.getMessage());
        return new ApiResponse(false, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse handleUnexpected(Exception ex) {
        log.error("Непредвиденная ошибка", ex);
        return new ApiResponse(false, "Внутренняя ошибка сервера");
    }
}
