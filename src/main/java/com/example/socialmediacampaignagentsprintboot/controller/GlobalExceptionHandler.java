package com.example.socialmediacampaignagentsprintboot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * A global exception handler for managing and logging exceptions that occur
 * across the application. This class centralizes exception handling and
 * provides user-friendly error messages through ModelAndView objects.
 * It supports the handling of general exceptions and specific validation errors.
 * <p>
 * The following exception types are handled:
 * - {@link Exception}: Captures all types of exceptions not specifically handled elsewhere.
 * - {@link IllegalArgumentException}: Captures input validation errors.
 * <p>
 * This handler ensures proper logging and redirection to pre-defined error views.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(Exception ex) {
        log.error("❌ Global Error Caught: {}", ex.getMessage(), ex);

        // Create a ModelAndView to route the user back to the starting page
        ModelAndView mav = new ModelAndView("init");
        mav.addObject("errorMessage", "An unexpected error occurred. Please try again later.");

        return mav;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleValidationErrors(IllegalArgumentException ex) {
        log.warn("⚠️ Validation Error: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("init");
        mav.addObject("errorMessage", "Invalid input provided. Please review your request and try again.");
        return mav;
    }
}