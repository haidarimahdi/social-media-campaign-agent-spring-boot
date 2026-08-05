package com.example.socialmediacampaignagentsprintboot.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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


    /**
     * Handles specific LLM API failures (like the 429 Resource Exhausted error).
     * This provides transparent HITL feedback that their progress is safe.
     */
    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeExceptions(RuntimeException ex, HttpServletRequest request) {
        log.error("❌ Runtime Error Caught: {}", ex.getMessage(), ex);

        // Check if the error is related to Vertex AI / LangChain4j rate limits
        boolean isLlmError = ex.getMessage().contains("RESOURCE_EXHAUSTED") ||
                ex.getMessage().contains("429") ||
                ex.getMessage().contains("AI failed");

        String userMessage = isLlmError
                ? "The AI service is currently overloaded or timed out. Don't worry, your progress up to this point has been saved."
                : "An unexpected runtime error occurred.";

        return routeToRecovery(request, userMessage);
    }

    /**
     * Captures all other types of exceptions not specifically handled elsewhere.
     */
    @ExceptionHandler(Exception.class)
    public Object handleAllExceptions(Exception ex, HttpServletRequest request) {
        log.error("❌ Global Error Caught: {}", ex.getMessage(), ex);
        return routeToRecovery(request, "A critical system error occurred. Please check your drafts to resume progress.");
    }

    /**
     * Handles Expected Negative Paths (Validation).
     * Routes the user back to the starting form to correct their input.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleValidationErrors(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("⚠️ Validation Error: {}", ex.getMessage());

        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Invalid input: " + ex.getMessage() + "\"}");
        }

        // Return the user straight to the initialization form to try a new prompt
        ModelAndView mav = new ModelAndView("init");
        mav.addObject("errorMessage", "Validation Failed: " + ex.getMessage());
        return mav;
    }

    /**
     * Helper method to route the error properly based on the type of request.
     */
    private Object routeToRecovery(HttpServletRequest request, String message) {
        // If the request was an API call (e.g., from a fetch() script expecting JSON)
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + message + "\"}");
        }

        // Otherwise, ModelAndView for HTML error page
        ModelAndView mav = new ModelAndView("error-recovery");
        mav.addObject("errorMessage", message);
        return mav;
    }
}