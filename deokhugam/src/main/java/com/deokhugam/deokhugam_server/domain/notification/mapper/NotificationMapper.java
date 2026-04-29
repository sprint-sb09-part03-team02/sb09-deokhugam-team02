package com.deokhugam.deokhugam_server.domain.notification.mapper;

import com.deokhugam.deokhugam_server.domain.notification.dto.response.NotificationDto;
import com.deokhugam.deokhugam_server.domain.notification.entity.Notification;
import com.deokhugam.deokhugam_server.domain.review.entity.Review;
import com.deokhugam.deokhugam_server.domain.review.repository.ReviewRepository;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class NotificationMapper {

    @Autowired
    protected ReviewRepository reviewRepository;

    @Mapping(target = "reviewContent", expression = "java(getReviewContent(notification.getReviewId()))")
    @Mapping(target = "message", source = "content")
    @Mapping(target = "confirmed", expression = "java(notification.isRead())")
    public abstract NotificationDto toDto(Notification notification);

    protected String getReviewContent(UUID reviewId) {
        return reviewRepository.findByIdAndIsDeletedFalse(reviewId)
            .map(Review::getContent)
            .orElse(null);
    }
}
