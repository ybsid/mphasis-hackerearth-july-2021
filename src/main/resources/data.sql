DROP TABLE IF EXISTS books;

CREATE TABLE books (
  bookId INT  PRIMARY KEY,
  title VARCHAR(250) ,
  authors VARCHAR(500) ,
  average_rating VARCHAR(250),
  isbn varchar(250),
  language_code varchar(250),
  ratings_count varchar(250),
  price int
);