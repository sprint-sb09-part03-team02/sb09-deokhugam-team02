package com.deokhugam.deokhugam_server.domain.book.entity;

import com.deokhugam.deokhugam_server.global.entity.BaseEntity;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseEntity {

  private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");
  private static final int HANGUL_BASE = 0xAC00;
  private static final int HANGUL_END = 0xD7A3;
  private static final int HANGUL_JUNG_COUNT = 21;
  private static final int HANGUL_JONG_COUNT = 28;

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "title_sort_key", nullable = false, length = 500)
  private String titleSortKey;

  @Column(name = "author", nullable = false, length = 255)
  private String author;

  @Column(name = "isbn", nullable = false, unique = true, length = 20)
  private String isbn;

  @Column(name = "publisher", length = 255)
  private String publisher;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Column(name = "thumbnail_url", length = 1000)
  private String thumbnailUrl;

  @Column(name = "published_date", nullable = false)
  private LocalDate publishedDate;

  @Column(name = "review_count", nullable = false)
  private int reviewCount;

  @Column(name = "rating", nullable = false)
  private double rating;

  public Book(
    String title,
    String author,
    String isbn,
    String publisher,
    String description,
    String thumbnailUrl,
    LocalDate publishedDate
  ) {
    this.title = title;
    this.titleSortKey = toTitleSortKey(title);
    this.author = author;
    this.isbn = isbn;
    this.publisher = publisher;
    this.description = description;
    this.thumbnailUrl = thumbnailUrl;
    this.publishedDate = publishedDate;
    this.reviewCount = 0;
    this.rating = 0.0;
  }

  public void update(
    String title,
    String author,
    String publisher,
    String description,
    String thumbnailUrl,
    LocalDate publishedDate
  ) {
    if (title != null) {
      this.title = title;
      this.titleSortKey = toTitleSortKey(title);
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
    if (thumbnailUrl != null) {
      this.thumbnailUrl = thumbnailUrl;
    }
    if (publishedDate != null) {
      this.publishedDate = publishedDate;
    }
  }

  public void updateReviewStatistics(int reviewCount, double rating) {
    this.reviewCount = reviewCount;
    this.rating = rating;
  }

  public static String toTitleSortKey(String title) {
    if (title == null || title.isBlank()) {
      return "";
    }

    String normalized = Normalizer.normalize(title.trim(), Normalizer.Form.NFKC)
      .toLowerCase(Locale.ROOT)
      .replaceAll("[\\p{Punct}\\p{IsPunctuation}]+", " ")
      .replaceAll("\\s+", " ")
      .trim();

    Matcher matcher = DIGIT_PATTERN.matcher(normalized);
    StringBuilder sortKey = new StringBuilder();

    while (matcher.find()) {
      matcher.appendReplacement(sortKey, padNumber(matcher.group()));
    }
    matcher.appendTail(sortKey);

    return normalizeHangulSyllables(sortKey.toString());
  }

  private static String normalizeHangulSyllables(String value) {
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < value.length(); i++) {
      char current = value.charAt(i);
      if (current >= HANGUL_BASE && current <= HANGUL_END) {
        int syllableIndex = current - HANGUL_BASE;
        int choseong = syllableIndex / (HANGUL_JUNG_COUNT * HANGUL_JONG_COUNT);
        int jungseong = (syllableIndex % (HANGUL_JUNG_COUNT * HANGUL_JONG_COUNT)) / HANGUL_JONG_COUNT;
        int jongseong = syllableIndex % HANGUL_JONG_COUNT;
        result.append('k')
          .append(twoDigit(choseong))
          .append(twoDigit(jungseong))
          .append(twoDigit(jongseong));
      } else {
        result.append(current);
      }
    }

    return result.toString();
  }

  private static String twoDigit(int value) {
    return value < 10 ? "0" + value : String.valueOf(value);
  }

  private static String padNumber(String value) {
    String normalizedNumber = value.replaceFirst("^0+(?!$)", "");
    if (normalizedNumber.length() >= 20) {
      return normalizedNumber;
    }
    return "0".repeat(20 - normalizedNumber.length()) + normalizedNumber;
  }
}
