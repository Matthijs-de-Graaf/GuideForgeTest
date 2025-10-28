CREATE TABLE book (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  author VARCHAR(255),
  isbn VARCHAR(50),
  published_date DATE
);