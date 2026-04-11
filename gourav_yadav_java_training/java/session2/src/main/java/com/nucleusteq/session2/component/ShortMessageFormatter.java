package com.nucleusteq.session2.component;

import org.springframework.stereotype.Component;

// Marks this class as a Spring-managed component
// Spring will automatically detect it and include it in dependency injection
@Component
public class ShortMessageFormatter implements MessageFormatter {
    @Override
    public String format() {
        return "Hi! This is a short message.";
    }

    // This method returns the type handled by this formatter
    // It is used as a key when building the Map in MessageService

    @Override
    public String getType() {
        return "SHORT";
    }
}