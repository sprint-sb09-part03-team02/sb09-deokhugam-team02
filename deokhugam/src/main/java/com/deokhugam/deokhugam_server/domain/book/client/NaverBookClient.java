package com.deokhugam.deokhugam_server.domain.book.client;

import com.deokhugam.deokhugam_server.domain.book.config.NaverApiProperties;
import com.deokhugam.deokhugam_server.domain.book.dto.response.NaverBookDto;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class NaverBookClient implements BookInfoClient {

  private static final DateTimeFormatter NAVER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

  private final RestClient.Builder restClientBuilder;
  private final NaverApiProperties properties;

  @Override
  public NaverBookDto searchByIsbn(String isbn) {
    try {
      NaverBookResponse response = restClientBuilder.build()
        .get()
        .uri(properties.url() + "?d_isbn={isbn}", isbn)
        .header("X-Naver-Client-Id", properties.clientId())
        .header("X-Naver-Client-Secret", properties.clientSecret())
        .retrieve()
        .body(NaverBookResponse.class);

      if (response == null || response.items().isEmpty()) {
        throw new DeokhugamException(ErrorCode.BOOK_INFO_NOT_FOUND);
      }

      return response.items().stream()
        .filter(item -> item.normalizedIsbn().contains(isbn))
        .findFirst()
        .or(() -> response.items().stream().findFirst())
        .map(this::toDto)
        .orElseThrow(() -> new DeokhugamException(ErrorCode.BOOK_INFO_NOT_FOUND));
    } catch (DeokhugamException e) {
      throw e;
    } catch (Exception e) {
      throw new DeokhugamException(ErrorCode.BOOK_INFO_NOT_FOUND);
    }
  }

  private NaverBookDto toDto(NaverBookItem item) {
    return new NaverBookDto(
      clean(item.title()),
      clean(item.author()),
      clean(item.description()),
      clean(item.publisher()),
      parsePublishedDate(item.pubdate()),
      item.normalizedIsbn(),
      encodeImageAsBase64(item.image())
    );
  }

  private String encodeImageAsBase64(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) {
      return null;
    }

    try {
      byte[] imageBytes = restClientBuilder.build()
        .get()
        .uri(imageUrl)
        .retrieve()
        .body(byte[].class);

      return imageBytes == null || imageBytes.length == 0
        ? null
        : Base64.getEncoder().encodeToString(imageBytes);
    } catch (Exception e) {
      return null;
    }
  }

  private LocalDate parsePublishedDate(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return LocalDate.parse(value, NAVER_DATE_FORMAT);
  }

  private String clean(String value) {
    if (value == null) {
      return null;
    }
    return HTML_TAG_PATTERN.matcher(value).replaceAll("").trim();
  }

  private record NaverBookResponse(
    List<NaverBookItem> items
  ) {
    private NaverBookResponse {
      items = items == null ? List.of() : items;
    }
  }

  private record NaverBookItem(
    String title,
    String image,
    String author,
    String publisher,
    String pubdate,
    String isbn,
    String description
  ) {
    private String normalizedIsbn() {
      return isbn == null ? "" : isbn.replaceAll("[^0-9Xx]", "");
    }
  }
}
