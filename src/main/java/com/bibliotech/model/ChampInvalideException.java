package com.bibliotech.model;

public class ChampInvalideException extends IllegalArgumentException {

    private final String champ;

    public ChampInvalideException(String champ, String message) {
        super(message);
        this.champ = champ;
    }

    public String getChamp() {
        return champ;
    }
}