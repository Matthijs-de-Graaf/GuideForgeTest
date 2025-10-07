package com.guideforge.backend.book;

import java.util.List;

// Wrapper voor de JSON data in de API, e.g.
//
// MET Wrapper:
//
// {
//  "books": [
//    { "id": 1, "title": "Book 1", ... },
//    { "id": 2, "title": "Book 2", ... }
//  ]
// }

// ZONDER Wrapper:
//
// [
//   { "id": 1, "title": "Book 1", ... },
//   { "id": 2, "title": "Book 2", ... }
// ]


public record Books(List<Book> books) {}
