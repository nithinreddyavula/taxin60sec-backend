package com.taxin60sec.backend.mapper;

import com.taxin60sec.backend.dto.domain.TimelineEventDto;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.TimelineEvent;
import com.taxin60sec.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TimelineEventMapper {
    public TimelineEventDto toDto(TimelineEvent event) {
        return new TimelineEventDto(
                event.getId(),
                event.getEventType(),
                event.getTitle(),
                event.getDescription(),
                idOf(event.getTaxCase()),
                idOf(event.getActor()),
                event.getCreatedAt()
        );
    }

    private Long idOf(Case taxCase) {
        return taxCase == null ? null : taxCase.getId();
    }

    private Long idOf(User user) {
        return user == null ? null : user.getId();
    }
}
