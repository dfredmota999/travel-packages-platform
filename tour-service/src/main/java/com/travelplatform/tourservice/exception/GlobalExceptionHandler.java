package com.travelplatform.tourservice.exception;

import com.travelplatform.tourservice.domain.InsufficientAvailabilityException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TourOfferNotFoundException.class)
    public ProblemDetail handleOfferNotFound(TourOfferNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Oferta não encontrada");
        problem.setType(URI.create("https://travelplatform.com/problems/tour-offer-not-found"));
        return problem;
    }

    @ExceptionHandler(TourReservationNotFoundException.class)
    public ProblemDetail handleReservationNotFound(TourReservationNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Reserva não encontrada");
        problem.setType(URI.create("https://travelplatform.com/problems/tour-reservation-not-found"));
        return problem;
    }

    @ExceptionHandler(InsufficientAvailabilityException.class)
    public ProblemDetail handleInsufficientAvailability(InsufficientAvailabilityException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Vagas insuficientes");
        problem.setType(URI.create("https://travelplatform.com/problems/insufficient-availability"));
        return problem;
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "A oferta foi alterada por outra requisição concorrente. Tente novamente.");
        problem.setTitle("Conflito de concorrência");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Requisição inválida");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Erro de validação nos campos enviados");
        problem.setTitle("Validação falhou");
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList());
        return problem;
    }
}
