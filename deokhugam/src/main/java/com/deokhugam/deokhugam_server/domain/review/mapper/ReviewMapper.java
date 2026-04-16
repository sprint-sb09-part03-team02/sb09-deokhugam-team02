package com.deokhugam.deokhugam_server.domain.review.mapper;

import static com.deokhugam.deokhugam_server.domain.book.entity.QBook.book;
import static com.deokhugam.deokhugam_server.domain.user.entity.QUser.user;

import com.deokhugam.deokhugam_server.domain.book.entity.Book;
import com.deokhugam.deokhugam_server.domain.review.dto.response.PopularReviewDto;
import com.deokhugam.deokhugam_server.domain.review.entity.PopularReview;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.user.dto.response.PowerUserDto;
import com.deokhugam.deokhugam_server.domain.user.entity.PowerUser;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
  public static PopularReviewDto toPopularDto(PopularReview entity, Book book, User user) {
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
