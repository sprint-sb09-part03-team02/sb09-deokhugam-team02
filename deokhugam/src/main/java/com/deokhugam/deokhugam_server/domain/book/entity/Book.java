package com.deokhugam.deokhugam_server.domain.book.entity;

import com.deokhugam.deokhugam_server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Column(name = "isbn", unique = true, length = 20)
    private String isbn;

    @Column(name = "publisher", length = 100)
    private String publisher;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "published_at")
    private LocalDate publishedAt;

    public Book(
            String title,
            String author,
            String isbn,
            String publisher,
            String description,
            String imageUrl,
            LocalDate publishedAt
    ) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
        this.description = description;
        this.imageUrl = imageUrl;
        this.publishedAt = publishedAt;
    }

    public void update(
            String title,
            String author,
            String publisher,
            String description,
            String imageUrl,
            LocalDate publishedAt
    ) {
        if (title != null) {
            this.title = title;
        }

        if (author != null) {
            this.author = author;
        }

        if (publisher != null) {
            this.publisher = publisher;
        }

        if (description != null) {
            this.description = description;
        }

        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }

        if (publishedAt != null) {
            this.publishedAt = publishedAt;
        }
    }
}