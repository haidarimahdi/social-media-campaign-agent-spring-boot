package com.example.socialmediacampaignagentsprintboot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

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