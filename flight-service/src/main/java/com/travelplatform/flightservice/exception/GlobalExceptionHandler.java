package com.travelplatform.flightservice.exception;

import com.travelplatform.flightservice.domain.InsufficientAvailabilityException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FlightOfferNotFoundException.class)
    public ProblemDetail handleOfferNotFound(FlightOfferNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Oferta não encontrada");
        problem.setType(URI.create("https://travelplatform.com/problems/flight-offer-not-found"));
        return problem;
    }

    @ExceptionHandler(FlightReservationNotFoundException.class)
    public ProblemDetail handleReservationNotFound(FlightReservationNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Reserva não encontrada");
        problem.setType(URI.create("https://travelplatform.com/problems/flight-reservation-not-found"));
        return problem;
    }

    /** Sem assentos suficientes -> 409 Conflict (o recurso existe, mas o pedido conflita com o estado atual). */
    @ExceptionHandler(InsufficientAvailabilityException.class)
    public ProblemDetail handleInsufficientAvailability(InsufficientAvailabilityException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Assentos insuficientes");
        problem.setType(URI.create("https://travelplatform.com/problems/insufficient-availability"));
        return problem;
    }

    /** Conflito de concorrência detectado pelo lock otimista (@Version). */
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
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
