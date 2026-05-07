package com.deokhugam.deokhugam_server.domain.review.mapper;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.review.dto.request.ReviewCreateRequest;
import com.deokhugam.deokhugam_server.domain.review.dto.response.PopularReviewDto;
import com.deokhugam.deokhugam_server.domain.review.dto.response.ReviewDto;
import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.global.mapper.StaticImagePathMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    uses = StaticImagePathMapper.class,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ReviewMapper {

  // 1. Review -> ReviewDto
  @Mapping(target = "bookId", source = "review.book.id")
  @Mapping(target = "bookTitle", source = "review.book.title")
  @Mapping(target = "bookThumbnailUrl", source = "review.book.thumbnailUrl", qualifiedByName = "normalizeStaticImagePath")
  @Mapping(target = "userId", source = "review.user.id")
  @Mapping(target = "userNickname", source = "review.user.nickname")
  @Mapping(target = "likedByMe", source = "likedByMe")
  ReviewDto toDto(Review review, boolean likedByMe);

  // 2. Request -> Entity
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "book", source = "book")
  @Mapping(target = "user", source = "user")
  @Mapping(target = "content", source = "request.content")
  @Mapping(target = "rating", source = "request.rating")
  @Mapping(target = "likeCount", ignore = true)
  @Mapping(target = "commentCount", ignore = true)
  @Mapping(target = "likes", ignore = true)
  @Mapping(target = "comments", ignore = true)
  Review toEntity(ReviewCreateRequest request, Book book, User user);

  // 3. PopularReview -> PopularReviewDto
  @Mapping(target = "bookId", source = "popularReview.review.book.id")
  @Mapping(target = "bookTitle", source = "popularReview.review.book.title")
  @Mapping(target = "bookThumbnailUrl", source = "popularReview.review.book.thumbnailUrl", qualifiedByName = "normalizeStaticImagePath")
  @Mapping(target = "userId", source = "popularReview.review.user.id")
  @Mapping(target = "userNickname", source = "popularReview.review.user.nickname")
  @Mapping(target = "reviewId", source = "popularReview.review.id")
  @Mapping(target = "reviewContent", source = "popularReview.review.content")
  @Mapping(target = "reviewRating", source = "popularReview.review.rating")
  @Mapping(target = "period", source = "popularReview.periodType") // period -> periodType
  @Mapping(target = "createdAt", source = "popularReview.review.createdAt")
  @Mapping(target = "rank", source = "popularReview.rankOrder")    // rank -> rankOrder
  PopularReviewDto toPopularDto(PopularReview popularReview);
}
