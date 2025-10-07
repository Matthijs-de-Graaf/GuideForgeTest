package com.guideforge.backend.book;

// Object van Book
// Objecten Author & Publisher worden hieraan toegevoegd
// Language is een ENUM type
public record Book(
    Integer id,
    String title,
    String isbn,
    String publication_date,
    Integer pages,
    Language language,
    Author author,
    Publisher publisher
) {}
