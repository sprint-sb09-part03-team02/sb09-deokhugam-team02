package com.deokhugam.deokhugam_server.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentCreateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentSearchRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.deokhugam_server.domain.comment.dto.response.CommentDto;
import com.deokhugam.deokhugam_server.domain.comment.entity.Comment;
import com.deokhugam.deokhugam_server.domain.comment.event.CommentCreatedEvent;
import com.deokhugam.deokhugam_server.domain.comment.mapper.CommentMapper;
import com.deokhugam.deokhugam_server.domain.comment.repository.CommentRepository;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import com.deokhugam.deokhugam_server.domain.user.entity.User;
import com.deokhugam.deokhugam_server.domain.user.repository.UserRepository;
import com.deokhugam.deokhugam_server.global.exception.DeokhugamException;
import com.deokhugam.deokhugam_server.global.exception.ErrorCode;
import com.deokhugam.deokhugam_server.global.response.CursorPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

  @InjectMocks private CommentServiceImpl commentService;

  @Mock private CommentRepository commentRepository;
  @Mock private ReviewRepository reviewRepository;
  @Mock private UserRepository userRepository;
  @Mock private CommentMapper commentMapper;
  @Mock private ApplicationEventPublisher eventPublisher;

  private User user;
  private Review review;
  private Comment comment;
  private UUID userId = UUID.randomUUID();
  private UUID reviewId = UUID.randomUUID();
  private UUID commentId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    user = User.builder().email("test@test.com").nickname("민주").build();
    ReflectionTestUtils.setField(user, "id", userId);

    review = Review.builder().content("리뷰 내용").build();
    ReflectionTestUtils.setField(review, "id", reviewId);

    comment = Comment.builder().reviewId(reviewId).userId(userId).content("댓글 내용").build();
    ReflectionTestUtils.setField(comment, "id", commentId);
    ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.now());
  }

  @Nested
  @DisplayName("댓글 생성 테스트")
  class CreateComment {
    @Test
    @DisplayName("성공: 댓글 생성 시 리뷰의 댓글 수가 증가하고 이벤트가 발행된다")
    void createComment_Success() {
      // given
      CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "새 댓글");
      int initialCount = review.getCommentCount();

      given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(commentRepository.save(any(Comment.class))).willReturn(comment);
      given(commentMapper.toDto(any(), anyString())).willReturn(mock(CommentDto.class));

      // when
      commentService.createComment(request);

      // then
      assertThat(review.getCommentCount()).isEqualTo(initialCount + 1); // 댓글 수 증가 확인
      verify(eventPublisher).publishEvent(any(CommentCreatedEvent.class)); // 이벤트 발행 확인
      verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("실패: 리뷰를 찾을 수 없으면 예외 발생")
    void createComment_Fail_ReviewNotFound() {
      given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

      assertThatThrownBy(() -> commentService.createComment(new CommentCreateRequest(reviewId, userId, "내용")))
          .isInstanceOf(DeokhugamException.class)
          .hasMessageContaining(ErrorCode.REVIEW_NOT_FOUND.getMessage());
    }
  }

  @Nested
  @DisplayName("댓글 수정 및 조회")
  class UpdateAndRead {
    @Test
    @DisplayName("성공: 작성자가 수정을 요청하면 내용이 변경된다")
    void updateComment_Success() {
      // given
      CommentUpdateRequest request = new CommentUpdateRequest("수정된 내용");
      given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
      given(userRepository.findById(userId)).willReturn(Optional.of(user));

      // when
      commentService.updateComment(commentId, userId, request);

      // then
      assertThat(comment.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("실패: 작성자가 아닌 유저가 수정하면 예외 발생")
    void updateComment_Fail_NotOwner() {
      UUID otherUserId = UUID.randomUUID();
      given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

      assertThatThrownBy(() -> commentService.updateComment(commentId, otherUserId, new CommentUpdateRequest("내용")))
          .isInstanceOf(DeokhugamException.class)
          .hasMessageContaining(ErrorCode.NOT_COMMENT_OWNER.getMessage());
    }

    @Test
    @DisplayName("성공: 댓글 상세 조회")
    void getComment_Success() {
      given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(commentMapper.toDto(any(), anyString())).willReturn(mock(CommentDto.class));

      CommentDto result = commentService.getComment(commentId);
      assertThat(result).isNotNull();
    }
  }

  @Nested
  @DisplayName("댓글 삭제 테스트")
  class DeleteComment {
    @Test
    @DisplayName("성공: 논리 삭제 시 리뷰의 댓글 수가 감소한다")
    void deleteComment_Success() {
      // given
      review.increaseCommentCount(); // 테스트 전 카운트 1로 설정
      given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
      given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

      // when
      commentService.deleteComment(commentId, userId);

      // then
      assertThat(review.getCommentCount()).isZero();
      // comment.isDeleted() 검증은 BaseEntity 로직이므로 생략하거나 필요시 추가
    }

    @Test
    @DisplayName("성공: 물리 삭제 시 리포지토리의 delete가 호출된다")
    void permanentDeleteComment_Success() {
      // given
      review.increaseCommentCount();
      given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
      given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

      // when
      commentService.permanentDeleteComment(commentId, userId);

      // then
      verify(commentRepository).delete(comment);
      assertThat(review.getCommentCount()).isZero();
    }
  }

  @Test
  @DisplayName("성공: 리뷰 ID로 댓글 목록을 조회한다 (페이징)")
  void getCommentsByReviewId_Success() {
    // given
    CommentSearchRequest request = new CommentSearchRequest();
    request.setReviewId(reviewId);
    request.setLimit(10);

    CommentDto dto = new CommentDto(commentId, reviewId, userId, "닉네임", "내용", LocalDateTime.now(), null);
    given(commentRepository.searchComments(any())).willReturn(List.of(dto));
    given(commentRepository.countComments(reviewId)).willReturn(1L);

    // when
    CursorPageResponse<CommentDto> response = commentService.getCommentsByReviewId(request);

    // then
    assertThat(response.content()).hasSize(1);
    verify(commentRepository).searchComments(request);
  }
}