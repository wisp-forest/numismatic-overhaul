package com.glisco.numismaticoverhaul.villagers;

public class DeserializationException extends RuntimeException {

    private final DeserializationContext context;

    public DeserializationException(String message) {
        super(message);
        this.context = DeserializationContext.getCurrentState();
    }

    public DeserializationContext getContext() {
        return context;
    }

}
