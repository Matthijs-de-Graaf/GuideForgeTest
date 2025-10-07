package com.guideforge.backend.book;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // Return 404
public class BookNotFoundException extends RuntimeException 
{
    public BookNotFoundException()
    {
        super("Book not found!");
    }
}
