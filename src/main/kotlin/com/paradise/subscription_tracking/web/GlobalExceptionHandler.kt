package com.paradise.subscription_tracking.web

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.paradise.subscription_tracking.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Перехват ошибок валидации Spring (из аннотаций @field: в DTO)
     * Возвращает статус 400 Bad Request со списком всех невалидных полей.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors.associate { error ->
            error.field to (error.defaultMessage ?: "Invalid value")
        }

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "Validation failed",
            path = request.requestURI,
            details = details
        )
        return ResponseEntity.badRequest().body(errorResponse)
    }

    /**
     * Перехват ошибок бизнес-логики из блока init в DTO (IllegalArgumentException)
     * и некорректных переходов по жизненному циклу (IllegalStateException)
     * Возвращает статус 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    fun handleBadRequestExceptions(
        ex: RuntimeException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message ?: "Invalid request data",
            path = request.requestURI
        )
        return ResponseEntity.badRequest().body(errorResponse)
    }

    /**
     * Перехват ситуации, когда подписка не найдена в БД (NoSuchElementException)
     * Возвращает статус 404 Not Found.
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFoundException(
        ex: NoSuchElementException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = HttpStatus.NOT_FOUND.reasonPhrase,
            message = ex.message ?: "Resource not found",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    /**
     * Обработка некорректного JSON или ошибок десериализации Jackson.
     * Перехватывает системные сбои парсинга (MismatchedInputException) до этапа валидации
     * и извлекает имя пропущенного/невалидного поля для формирования понятного JSON-ответа.
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val rootCause = ex.rootCause
        val message = if (rootCause is MismatchedInputException) {
            val fieldName = rootCause.path.joinToString(".") { it.fieldName ?: "" }
            "Required field '$fieldName' is missing or has an invalid format"
        } else {
            "Malformed JSON request"
        }

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = message,
            path = request.requestURI
        )
        return ResponseEntity.badRequest().body(errorResponse)
    }
}
