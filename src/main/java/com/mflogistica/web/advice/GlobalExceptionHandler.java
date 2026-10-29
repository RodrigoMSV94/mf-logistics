package com.mflogistica.web.advice;

import com.mflogistica.domain.exception.BusinessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException exception, Model model) {
        model.addAttribute("errorTitle", "Error de negocio");
        model.addAttribute("errorMessage", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpectedException(Exception exception, Model model) {
        model.addAttribute("errorTitle", "Error inesperado");
        model.addAttribute("errorMessage", "Ha ocurrido un error no controlado. Intente nuevamente.");
        return "error";
    }
}

