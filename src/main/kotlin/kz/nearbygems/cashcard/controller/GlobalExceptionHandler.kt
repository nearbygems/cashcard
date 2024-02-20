package kz.nearbygems.cashcard.controller

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

  @ExceptionHandler(NotFoundException::class)
  protected fun handleNotFound(): ResponseEntity<Void> = ResponseEntity.notFound().build()

}