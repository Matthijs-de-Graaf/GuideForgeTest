package com.guideforge.backend.book;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Language 
{
    ENGLISH("English"),
    DUTCH("Dutch");

    private final String value;

    Language(String value)
    {
        this.value = value;
    }

    // Zorgt ervoor dat deze waarde als String in JSON verschijnt (bij serialisatie)
    @JsonValue
    public String getValue()
    {
        return value;
    }
}
