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

  // Request DTO -> Entity (UUID 기반이라 직접 매핑됨)
  Review toEntity(ReviewCreateRequest request);

  // Entity -> Response DTO
  // bookTitle, userNickname 등은 서비스에서 fetch해온 값을 넘겨받아 매핑
  @Mapping(target = "bookTitle", source = "bookTitle")
  @Mapping(target = "userNickname", source = "userNickname")
  @Mapping(target = "bookThumbnailUrl", source = "bookThumbnailUrl")
  @Mapping(target = "likedByMe", source = "likedByMe")
  ReviewDto toDto(Review review, String bookTitle, String bookThumbnailUrl, String userNickname, boolean likedByMe);

  default PopularReviewDto toPopularDto(PopularReview entity, Book book, User user) {
    Review review = entity.getReview();
    return new PopularReviewDto(
        entity.getId(),
        review.getId(),
        book.getId(),
        book.getTitle(),
        book.getImageUrl(),
        user.getId(),
        user.getNickname(),
        review.getContent(),
        (double) review.getRating(),
        entity.getPeriodType(),
        entity.getCreatedAt(),
        entity.getRankOrder(),
        entity.getScore(),
        entity.getLikeCount(),
        entity.getCommentCount()
    );
  }
}