package com.ybisd.mphasisbooksapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class MphasisBooksApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MphasisBooksApiApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}


@RestController
@Data
class BooksController {


    @Value("${external.api}") // reads external-books api from properties file
    private String apiUrl;

    private List<Books> booksList; // list of books from books-api , considering this as database

    @Autowired
    private RestTemplate template; // for calling books api


    @Autowired
    private BooksRepository booksRepository;


    @GetMapping("/")
    public List<Books> getAllBooks() {

        ResponseEntity<List<Books>> responseEntity = template.exchange(apiUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Books>>() {
                });

        booksList = responseEntity.getBody();

        List<BooksEntity> booksEntities = booksList.stream().map(e -> {

            BooksEntity entity = new BooksEntity();

            entity.setBookId(e.getBookId());
            entity.setTitle(e.getTitle());
            entity.setAuthors(e.getAuthors());
            entity.setAverageRating(e.getAverageRating());
            entity.setIsbn(e.getIsbn());
            entity.setLangCode(e.getLangCode());
            entity.setRatingsCount(e.getRatingsCount());
            entity.setPrice(e.getPrice());

            return entity;
        }).collect(Collectors.toList());

        booksRepository.saveAll(booksEntities);

        return booksList;
    }

    @GetMapping("/{bookID}")
    public Books getBookById(@PathVariable int bookID) throws NullPointerException {

        BooksEntity booksEntity = booksRepository.findById(bookID).orElse(null);


        assert booksEntity != null;
        return new Books(booksEntity.getBookId(), booksEntity.getTitle(), booksEntity.getAuthors(),
                booksEntity.getAverageRating(), booksEntity.getIsbn(), booksEntity.getLangCode(),
                booksEntity.getRatingsCount(), booksEntity.getPrice());

    }

    @GetMapping("/search/{term}")
    public List<Books> searchBooks(@PathVariable String term) {

        List<BooksEntity> entityList = booksRepository.searchBooks(term);

        return entityList
                .stream()
                .map(e -> new Books(e.getBookId(), e.getTitle(), e.getAuthors(), e.getAverageRating()
                        , e.getIsbn(), e.getLangCode(), e.getRatingsCount(), e.getPrice()))
                .collect(Collectors.toList());
    }
}

@Entity
@Table(name = "books") // refers to in memory db
@Data
class BooksEntity {

    @Id
    @Column(name = "bookId")
    private Integer bookId;

    @Column(name = "title", length = 70000)
    private String title;

    @Column(name = "authors", length = 1000)
    private String authors;

    @Column(name = "average_rating", length = 1000)
    private String averageRating;

    @Column(name = "isbn")
    private String isbn;

    @Column(name = "language_code")
    private String langCode;

    @Column(name = "ratings_count")
    private String ratingsCount;

    @Column(name = "price")
    private Integer price;

}


interface BooksRepository extends JpaRepository<BooksEntity, Integer> {

    @Query(nativeQuery = true, value = "select * from books where title like %?1%")
    List<BooksEntity> searchBooks(String term);

}


/**
 * uses lombok and jackson
 */
@Data
@AllArgsConstructor
class Books {

    @JsonProperty("bookID")
    private Integer bookId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("authors")
    private String authors;

    @JsonProperty("average_rating")
    private String averageRating;

    @JsonProperty("isbn")
    private String isbn;

    @JsonProperty("language_code")
    private String langCode;

    @JsonProperty("ratings_count")
    private String ratingsCount;

    @JsonProperty("price")
    private Integer price;


}