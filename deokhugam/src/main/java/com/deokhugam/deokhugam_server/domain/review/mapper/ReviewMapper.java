package com.deokhugam.deokhugam_server.domain.review.mapper;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.response.PopularReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewDto;
import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

  // 1. Review -> ReviewDto
  @Mapping(target = "bookId", source = "review.book.id")
  @Mapping(target = "bookTitle", source = "review.book.title")
  @Mapping(target = "bookThumbnailUrl", source = "review.book.thumbnailUrl")
  @Mapping(target = "userId", source = "review.user.id")
  @Mapping(target = "userNickname", source = "review.user.nickname")
  @Mapping(target = "likedByMe", source = "likedByMe")
  ReviewDto toDto(Review review, boolean likedByMe);

  // 2. Request -> Entity
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "book", source = "book")
  @Mapping(target = "user", source = "user")
  Review toEntity(ReviewCreateRequest request, Book book, User user);

  // 3. PopularReview -> PopularReviewDto
  @Mapping(target = "reviewId", source = "entity.review.id")
  @Mapping(target = "bookId", source = "book.id")
  @Mapping(target = "bookTitle", source = "book.title")
  @Mapping(target = "bookThumbnailUrl", source = "book.thumbnailUrl")
  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "userNickname", source = "user.nickname")
  @Mapping(target = "reviewContent", source = "entity.review.content")
  @Mapping(target = "reviewRating", source = "entity.review.rating")
  @Mapping(target = "period", source = "entity.periodType")
  @Mapping(target = "rank", source = "entity.rankOrder")
  PopularReviewDto toPopularDto(PopularReview entity, Book book, User user);
}