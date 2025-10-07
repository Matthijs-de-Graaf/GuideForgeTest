package com.guideforge.backend.book;

import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/books") // Default sub-URL
public class BookController 
{
    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository)
    {
        this.bookRepository = bookRepository;
    }

    /* Zonder Wrapper */
    @GetMapping("/all")
    List<Book> findAll()
    {
        return bookRepository.findAll();
    }

    /* Met Wrapper */
    // @GetMapping("/all")
    // Books findAll()
    // {
        // return new Books(bookRepository.findAll());
    // }


    @GetMapping("/{id}")
    Book findById(@PathVariable Integer id)
    {
        Optional<Book> book = bookRepository.findById(id);

        if (book.isEmpty())
        {
            // Komt een 404 i.p.v. 500
            throw new BookNotFoundException();
        }

        return book.get();
    }

    @ResponseStatus(HttpStatus.CREATED) // Geeft de 201 code als succes
    @PostMapping("/all")
    void create(@Valid @RequestBody Book book)
    {
        bookRepository.create(book);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT) // Geeft 204 terug 
    @PutMapping("/{id}")
    void update(@Valid @RequestBody Book book, @PathVariable Integer id)
    {
        bookRepository.update(book, id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    void delete(@PathVariable Integer id)
    {
        bookRepository.delete(id);
    }
}
