
-- -- #####################################
-- -- #   
-- -- #   - Wordt eenmalig uitgevoerd na aanmaken van database
-- -- #
-- -- #####################################

CREATE TABLE book
(
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    publication_date DATE,
    pages INT,
    language VARCHAR(20),

    author_name VARCHAR(100),
    author_birthdate DATE,
    author_country VARCHAR(50),

    publisher_name VARCHAR(100),
    publisher_city VARCHAR(50),
    publisher_country VARCHAR(50)
);