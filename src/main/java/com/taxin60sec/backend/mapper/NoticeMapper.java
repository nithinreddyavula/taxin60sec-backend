package com.taxin60sec.backend.mapper;

import com.taxin60sec.backend.dto.domain.NoticeDto;
import com.taxin60sec.backend.entity.Notice;
import org.springframework.stereotype.Component;

@Component
public class NoticeMapper {

    public NoticeDto toDto(Notice n) {
        return new NoticeDto(
                n.getId(),
                n.getType().name(),
                n.getSeverity().name(),
                n.getTitle(),
                n.getMessage(),
                n.getRelatedCase() != null ? n.getRelatedCase().getCaseNumber() : null,
                n.isRead(),
                n.getCreatedAt()
        );
    }
}