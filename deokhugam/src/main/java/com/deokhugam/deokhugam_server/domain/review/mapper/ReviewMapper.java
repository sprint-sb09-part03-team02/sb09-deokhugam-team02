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

  /**
   * Request DTO + 엔티티 객체 -> Review 엔티티
   * id 매핑 에러 해결: 빌더에 없는 id는 언급하지 않아도 자동으로 무시됨
   */
  @Mapping(target = "book", source = "book")
  @Mapping(target = "user", source = "user")
  @Mapping(target = "content", source = "request.content")
  @Mapping(target = "rating", source = "request.rating")
  Review toEntity(ReviewCreateRequest request, Book book, User user);

  /**
   * Entity -> Response DTO
   * 경로 에러 해결: 인자 이름인 'review'를 소스 경로 앞에 붙여줌
   */
  @Mapping(target = "userNickname", source = "review.user.nickname")
  @Mapping(target = "bookTitle", source = "review.book.title")
  @Mapping(target = "bookThumbnailUrl", source = "review.book.imageUrl")
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