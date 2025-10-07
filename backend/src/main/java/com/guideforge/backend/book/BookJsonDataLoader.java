package com.guideforge.backend.book;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

// Laad de JSON data in vanuit de "/data/books.json"
@Component
public class BookJsonDataLoader implements CommandLineRunner
{
    private static final Logger logger = LoggerFactory.getLogger(BookJsonDataLoader.class);
    private final BookRepository bookRepository;
    private final ObjectMapper objectMapper;

    public BookJsonDataLoader(BookRepository bookRepository, ObjectMapper objectMapper) 
    {
        this.bookRepository = bookRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception 
    {
        if (bookRepository.count() == 0) 
        {
            try (InputStream inputStream = getClass().getResourceAsStream("/data/books.json")) 
            {
                if (inputStream == null) 
                {
                    throw new IOException("JSON file not found: /data/books.json");
                }
                List<Book> books = objectMapper.readValue(inputStream, new TypeReference<List<Book>>() {});
                logger.info("Reading {} books from JSON data and saving to database.", books.size());
                bookRepository.saveAll(books);
            } 
            catch (IOException e) 
            {
                throw new RuntimeException("Failed to read JSON data", e);
            }
        } 
        else 
        {
            logger.info("Not loading Books from JSON data because the collection contains data.");
        }
    }
}