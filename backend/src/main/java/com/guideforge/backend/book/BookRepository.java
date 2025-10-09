package com.guideforge.backend.book;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
public class BookRepository 
{
    private static final Logger logger = LoggerFactory.getLogger(BookRepository.class);
    private final JdbcClient jdbcClient;

    // Dependecy Injection
    public BookRepository(JdbcClient jdbcClient)
    {
        this.jdbcClient = jdbcClient;
    }

    // Get all Books
    public List<Book> findAll()
    {
        return jdbcClient
        .sql("SELECT id, title, isbn, publication_date, pages, language, author_name, author_birthdate, author_country, publisher_name, publisher_city, publisher_country FROM book")
        .query((rs, rowNum) -> 
        {
            // Maak een Object <Author> van de query resultaten
            Author author = new Author(
                rs.getString("author_name"),
                rs.getString("author_birthdate"),
                rs.getString("author_country")
            );

            // Maak een Object <Publisher> van de query resultaten
            Publisher publisher = new Publisher(
                rs.getString("publisher_name"),
                rs.getString("publisher_city"),
                rs.getString("publisher_country")
            );

            // Enum type <Language> - Zet database string om naar Enum
            Language language = Language.valueOf(rs.getString("language"));

            // Return <Book> Object met bijgevoegd <Author> en <Publisher>
            return new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("isbn"),
                rs.getString("publication_date"),
                rs.getInt("pages"),
                language,
                author,
                publisher
            );
        })
        .list();
    }

    // Get Book by ID
    public Optional<Book> findById(Integer id) 
    {
        return jdbcClient
            .sql("SELECT id, title, isbn, publication_date, pages, language, author_name, author_birthdate, author_country, publisher_name, publisher_city, publisher_country FROM book WHERE id = :id")
            .param("id", id)
            .query((rs, rowNum) -> 
            {
                Author author = new Author(
                    rs.getString("author_name"),
                    rs.getString("author_birthdate"),
                    rs.getString("author_country")
                );

                Publisher publisher = new Publisher(
                    rs.getString("publisher_name"),
                    rs.getString("publisher_city"),
                    rs.getString("publisher_country")
                );

                // Enum type <Language> - Zet database string om naar Enum
                Language language = Language.valueOf(rs.getString("language"));

                return new Book(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("isbn"),
                    rs.getString("publication_date"),
                    rs.getInt("pages"),
                    language,
                    author,
                    publisher
                );
            })
            .optional();
    }

    // Create Book
    public void create(Book book)
    {
        var updated = jdbcClient.sql(
                "INSERT INTO book (title, isbn, publication_date, pages, language, author_name, author_birthdate, author_country, publisher_name, publisher_city, publisher_country) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            )
            .params(Arrays.asList(
                book.title(),
                book.isbn(),
                book.publication_date(),
                book.pages(),
                book.language().name(), // Haalt de naam als string op
                book.author() != null ? book.author().author_name() : null,
                book.author() != null ? book.author().author_birthdate() : null,
                book.author() != null ? book.author().author_country() : null,
                book.publisher() != null ? book.publisher().publisher_name() : null,
                book.publisher() != null ? book.publisher().publisher_city() : null,
                book.publisher() != null ? book.publisher().publisher_country() : null
            ))
            .update();

        Assert.state(updated == 1, "Failed to create book " + book.title());
    }

    // Update Book by ID
    public void update(Book book, Integer id) 
    {
        var updated = jdbcClient.sql(
            "UPDATE book SET title = ?, language = ? WHERE id = ?"
        )
        .params(List.of(
            book.title(), 
            book.language() != null ? book.language().name() : null, 
            id))
        .update();

        Assert.state(updated == 1, "Failed to update book " + book.title());
    }

    // Delete Book by ID
    public void delete(Integer id) 
    {
        var updated = jdbcClient.sql(
            "DELETE FROM book WHERE id = :id"
        )
        .param("id", id)
        .update();

        Assert.state(updated == 1, "Failed to delete book" + id);
    }
    
    // Get count of rows
    public int count() 
    {
        return jdbcClient
            .sql("SELECT * FROM book")
            .query()
            .listOfRows()
            .size();
    }

    // Create all books from the JSON data
    // - Used in <BookJsonDataLoader>
    public void saveAll(List<Book> books) 
    {
        books.stream()
            .forEach(this::create);
    }
}
