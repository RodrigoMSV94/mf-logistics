package com.mflogistica.web.advice;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void shouldReturnNotFoundForMissingStaticResources() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        NoResourceFoundException exception = new NoResourceFoundException(HttpMethod.GET, "favicon.ico");

        assertEquals(HttpStatus.NOT_FOUND, handler.handleMissingResource(exception).getStatusCode());
    }
}
