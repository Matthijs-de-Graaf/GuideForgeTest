package com.guideforge.backend;

import com.guideforge.backend.book.BookJsonDataLoader;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class Application 
{
    // Maak een logger om errors/data in de terminal te zien
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

    // Roep de run() method aan om de JSON te verwerken
    @Bean
    CommandLineRunner runner(BookJsonDataLoader bookJson)
    {
        return args ->
        {
            bookJson.run(args);
        };
    }
}